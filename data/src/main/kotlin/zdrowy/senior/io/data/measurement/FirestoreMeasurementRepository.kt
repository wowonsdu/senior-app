package zdrowy.senior.io.data.measurement

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentSnapshot
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.data.firestore.PatientUidProvider
import zdrowy.senior.io.data.firestore.toSingle
import zdrowy.senior.io.domain.history.ChartPoint
import zdrowy.senior.io.domain.history.ChartSeries
import zdrowy.senior.io.domain.history.DateRange
import zdrowy.senior.io.domain.history.HistoryFilterState
import zdrowy.senior.io.domain.history.HistoryRange
import zdrowy.senior.io.domain.measurement.Measurement
import zdrowy.senior.io.domain.measurement.MeasurementRepository
import zdrowy.senior.io.domain.measurement.MeasurementSource
import zdrowy.senior.io.domain.measurement.MeasurementType
import java.util.Locale

class FirestoreMeasurementRepository(
    private val uidProvider: PatientUidProvider
) : MeasurementRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun addMeasurement(
        type: MeasurementType,
        value: Double,
        timestamp: Long,
        source: MeasurementSource
    ): Single<String> {
        val uid = requireUid()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.MEASUREMENTS)

        return col.add(
            mapOf(
                "type" to type.name,
                "value" to value,
                "timestampMs" to timestamp,
                "source" to source.name,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).toSingle().map { it.id }
    }

    override fun addBloodPressureMeasurement(
        systolic: Int,
        diastolic: Int,
        timestamp: Long,
        source: MeasurementSource
    ): Single<String> {
        val uid = requireUid()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.MEASUREMENTS)

        return col.add(
            mapOf(
                "type" to MeasurementType.PRESSURE.name,
                "systolic" to systolic,
                "diastolic" to diastolic,
                "timestampMs" to timestamp,
                "source" to source.name,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).toSingle().map { it.id }
    }

    override fun getRecentMeasurements(
        limit: Int,
        types: List<MeasurementType>?
    ): Single<List<Measurement>> {
        if (types != null && types.isEmpty()) return Single.just(emptyList())

        val uid = requireUid()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.MEASUREMENTS)

        val query = buildMeasurementsQuery(
            base = col,
            types = types,
            dateRange = null
        )
            .orderBy("timestampMs", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        return query.get()
            .toSingle()
            .map { snapshot -> snapshot.documents.map { it.toMeasurement() } }
    }

    override fun observeRecentMeasurements(
        limit: Int,
        types: List<MeasurementType>?
    ): Observable<List<Measurement>> {
        if (types != null && types.isEmpty()) return Observable.just(emptyList())

        val uid = requireUid()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.MEASUREMENTS)

        val query = buildMeasurementsQuery(
            base = col,
            types = types,
            dateRange = null
        )
            .orderBy("timestampMs", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        return observeMeasurements(query)
    }

    override fun getMeasurementHistory(
        types: List<MeasurementType>?,
        dateRange: DateRange?
    ): Single<List<Measurement>> {
        if (types != null && types.isEmpty()) return Single.just(emptyList())

        val uid = requireUid()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.MEASUREMENTS)

        val query = buildMeasurementsQuery(
            base = col,
            types = types,
            dateRange = dateRange
        ).orderBy("timestampMs", Query.Direction.DESCENDING)

        return query.get()
            .toSingle()
            .map { snapshot -> snapshot.documents.map { it.toMeasurement() } }
    }

    override fun observeMeasurementHistory(
        types: List<MeasurementType>?,
        dateRange: DateRange?
    ): Observable<List<Measurement>> {
        if (types != null && types.isEmpty()) return Observable.just(emptyList())

        val uid = requireUid()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.MEASUREMENTS)

        val query = buildMeasurementsQuery(
            base = col,
            types = types,
            dateRange = dateRange
        ).orderBy("timestampMs", Query.Direction.DESCENDING)

        return observeMeasurements(query)
    }

    override fun getMeasurementChartData(
        types: List<MeasurementType>?,
        dateRange: DateRange?
    ): Single<List<ChartSeries>> {
        return getMeasurementHistory(types, dateRange)
            .map { list ->
                val grouped = list.groupBy { it.type }
                grouped.map { (type, measurements) ->
                    val sorted = measurements.sortedBy { it.timestamp }
                    val points = sorted.mapNotNull { measurement ->
                        val value = measurement.value
                            ?: measurement.systolic?.toDouble()
                            ?: measurement.diastolic?.toDouble()
                        value?.let { ChartPoint(measurement.timestamp, it) }
                    }
                    val secondaryPoints = if (type == MeasurementType.PRESSURE) {
                        sorted.mapNotNull { measurement ->
                            measurement.diastolic?.toDouble()
                                ?.let { ChartPoint(measurement.timestamp, it) }
                        }
                    } else {
                        emptyList()
                    }
                    val primaryPoints = if (type == MeasurementType.PRESSURE) {
                        sorted.mapNotNull { measurement ->
                            measurement.systolic?.toDouble()
                                ?.let { ChartPoint(measurement.timestamp, it) }
                        }
                    } else {
                        points
                    }
                    ChartSeries(type, primaryPoints, secondaryPoints)
                }.sortedBy { it.type.name.lowercase(Locale.ROOT) }
            }
    }

    override fun observeMeasurementChartData(
        types: List<MeasurementType>?,
        dateRange: DateRange?
    ): Observable<List<ChartSeries>> {
        return observeMeasurementHistory(types, dateRange)
            .map { list ->
                val grouped = list.groupBy { it.type }
                grouped.map { (type, measurements) ->
                    val sorted = measurements.sortedBy { it.timestamp }
                    val points = sorted.mapNotNull { measurement ->
                        val value = measurement.value
                            ?: measurement.systolic?.toDouble()
                            ?: measurement.diastolic?.toDouble()
                        value?.let { ChartPoint(measurement.timestamp, it) }
                    }
                    val secondaryPoints = if (type == MeasurementType.PRESSURE) {
                        sorted.mapNotNull { measurement ->
                            measurement.diastolic?.toDouble()
                                ?.let { ChartPoint(measurement.timestamp, it) }
                        }
                    } else {
                        emptyList()
                    }
                    val primaryPoints = if (type == MeasurementType.PRESSURE) {
                        sorted.mapNotNull { measurement ->
                            measurement.systolic?.toDouble()
                                ?.let { ChartPoint(measurement.timestamp, it) }
                        }
                    } else {
                        points
                    }
                    ChartSeries(type, primaryPoints, secondaryPoints)
                }.sortedBy { it.type.name.lowercase(Locale.ROOT) }
            }
    }

    override fun getHistoryFilters(): Single<HistoryFilterState> {
        val types = MeasurementType.values().toList()
        val ranges = HistoryRange.values().toList()
        return Single.just(
            HistoryFilterState(
                availableTypes = types,
                availableRanges = ranges,
                selectedTypes = types,
                selectedRange = HistoryRange.LAST_30_DAYS
            )
        )
    }

    override fun updateMeasurement(
        id: String,
        type: MeasurementType,
        value: Double,
        timestamp: Long
    ): Single<Unit> {
        val uid = requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.MEASUREMENTS)
            .document(id)

        return doc.update(
            mapOf(
                "type" to type.name,
                "value" to value,
                "timestampMs" to timestamp,
                "systolic" to FieldValue.delete(),
                "diastolic" to FieldValue.delete()
            )
        ).toSingle().map { Unit }
    }

    override fun updateBloodPressureMeasurement(
        id: String,
        systolic: Int,
        diastolic: Int,
        timestamp: Long
    ): Single<Unit> {
        val uid = requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.MEASUREMENTS)
            .document(id)

        return doc.update(
            mapOf(
                "type" to MeasurementType.PRESSURE.name,
                "systolic" to systolic,
                "diastolic" to diastolic,
                "timestampMs" to timestamp,
                "value" to FieldValue.delete()
            )
        ).toSingle().map { Unit }
    }

    override fun deleteMeasurement(id: String): Single<Unit> {
        val uid = requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.MEASUREMENTS)
            .document(id)

        return doc.delete().toSingle().map { Unit }
    }

    private fun buildMeasurementsQuery(
        base: com.google.firebase.firestore.CollectionReference,
        types: List<MeasurementType>?,
        dateRange: DateRange?
    ): Query {
        var query: Query = base
        if (types != null) {
            query = query.whereIn("type", types.map { it.name })
        }
        dateRange?.from?.let { query = query.whereGreaterThanOrEqualTo("timestampMs", it) }
        dateRange?.to?.let { query = query.whereLessThanOrEqualTo("timestampMs", it) }
        return query
    }

    private fun DocumentSnapshot.toMeasurement(): Measurement {
        val typeRaw = getString("type").orEmpty()
        val type = runCatching { MeasurementType.valueOf(typeRaw) }.getOrDefault(MeasurementType.SUGAR)

        val sourceRaw = getString("source").orEmpty()
        val source = runCatching { MeasurementSource.valueOf(sourceRaw) }.getOrDefault(MeasurementSource.UNKNOWN)

        val value = (get("value") as? Number)?.toDouble()
        val systolic = (get("systolic") as? Number)?.toInt()
        val diastolic = (get("diastolic") as? Number)?.toInt()
        val timestamp = (get("timestampMs") as? Number)?.toLong() ?: 0L

        return Measurement(
            id = id,
            type = type,
            value = value,
            systolic = systolic,
            diastolic = diastolic,
            timestamp = timestamp,
            source = source
        )
    }

    private fun observeMeasurements(query: Query): Observable<List<Measurement>> {
        return Observable.create { emitter ->
            val registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!emitter.isDisposed) emitter.onError(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().map { it.toMeasurement() }
                if (!emitter.isDisposed) emitter.onNext(items)
            }
            emitter.setCancellable { registration.remove() }
        }
    }

    private fun requireUid(): String = uidProvider.requirePatientUid()
}

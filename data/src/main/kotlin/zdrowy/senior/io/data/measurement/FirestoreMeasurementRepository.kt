package zdrowy.senior.io.data.measurement

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentSnapshot
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.data.firestore.FirestorePaths
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

class FirestoreMeasurementRepository : MeasurementRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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

    override fun getMeasurementChartData(
        types: List<MeasurementType>?,
        dateRange: DateRange?
    ): Single<List<ChartSeries>> {
        return getMeasurementHistory(types, dateRange)
            .map { list ->
                val grouped = list.groupBy { it.type }
                grouped.map { (type, measurements) ->
                    val points = measurements
                        .sortedBy { it.timestamp }
                        .mapNotNull { measurement ->
                            val value = measurement.value
                                ?: measurement.systolic?.toDouble()
                                ?: measurement.diastolic?.toDouble()
                            value?.let { ChartPoint(measurement.timestamp, it) }
                        }
                    ChartSeries(type, points)
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

    private fun requireUid(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")
    }
}


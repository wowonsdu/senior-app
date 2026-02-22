package zdrowy.senior.io.data.measurement

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.reactivex.rxjava3.core.Observable
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.domain.measurement.Measurement
import zdrowy.senior.io.domain.measurement.MeasurementByUidRepository
import zdrowy.senior.io.domain.measurement.MeasurementSource
import zdrowy.senior.io.domain.measurement.MeasurementType

class FirestoreMeasurementByUidRepository : MeasurementByUidRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun observeRecentMeasurements(
        patientUid: String,
        limit: Int,
        types: List<MeasurementType>?
    ): Observable<List<Measurement>> {
        if (types != null && types.isEmpty()) return Observable.just(emptyList())

        val col = firestore.collection(FirestorePaths.USERS)
            .document(patientUid)
            .collection(FirestorePaths.MEASUREMENTS)

        val query = buildMeasurementsQuery(col, types)
            .orderBy("timestampMs", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        return observeMeasurements(query)
    }

    private fun buildMeasurementsQuery(
        base: com.google.firebase.firestore.CollectionReference,
        types: List<MeasurementType>?
    ): Query {
        var query: Query = base
        if (types != null) {
            query = query.whereIn("type", types.map { it.name })
        }
        return query
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
}

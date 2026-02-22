package zdrowy.senior.io.data.measurement

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.data.firestore.toCompletable
import zdrowy.senior.io.domain.measurement.MeasurementReadState
import zdrowy.senior.io.domain.measurement.MeasurementReadStateRepository
import zdrowy.senior.io.domain.user.CurrentUserUidProvider

class FirestoreMeasurementReadStateRepository(
    private val currentUserUidProvider: CurrentUserUidProvider
) : MeasurementReadStateRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun observeReadState(patientUid: String): Observable<MeasurementReadState> {
        val caregiverUid = currentUserUidProvider.requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(caregiverUid)
            .collection(FirestorePaths.MEASUREMENT_READ_STATES)
            .document(patientUid)

        return Observable.create { emitter ->
            val registration = doc.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!emitter.isDisposed) emitter.onError(error)
                    return@addSnapshotListener
                }
                val rawIds = snapshot?.get("readMeasurementIds") as? List<*>
                val ids = rawIds?.mapNotNull { it as? String }?.toSet().orEmpty()
                if (!emitter.isDisposed) {
                    emitter.onNext(MeasurementReadState(patientUid, ids))
                }
            }
            emitter.setCancellable { registration.remove() }
        }
    }

    override fun markMeasurementRead(patientUid: String, measurementId: String): Completable {
        val caregiverUid = currentUserUidProvider.requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(caregiverUid)
            .collection(FirestorePaths.MEASUREMENT_READ_STATES)
            .document(patientUid)

        val payload = mapOf(
            "patientUid" to patientUid,
            "readMeasurementIds" to FieldValue.arrayUnion(measurementId),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return doc.set(payload, SetOptions.merge()).toCompletable()
    }

    override fun setReadMeasurements(patientUid: String, measurementIds: List<String>): Completable {
        val caregiverUid = currentUserUidProvider.requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(caregiverUid)
            .collection(FirestorePaths.MEASUREMENT_READ_STATES)
            .document(patientUid)

        val payload = mapOf(
            "patientUid" to patientUid,
            "readMeasurementIds" to measurementIds.distinct(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return doc.set(payload, SetOptions.merge()).toCompletable()
    }
}

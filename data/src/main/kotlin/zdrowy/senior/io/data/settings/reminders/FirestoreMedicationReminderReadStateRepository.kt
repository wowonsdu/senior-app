package zdrowy.senior.io.data.settings.reminders

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.data.firestore.toCompletable
import zdrowy.senior.io.domain.settings.reminders.MedicationReminderReadState
import zdrowy.senior.io.domain.settings.reminders.MedicationReminderReadStateRepository
import zdrowy.senior.io.domain.user.CurrentUserUidProvider

class FirestoreMedicationReminderReadStateRepository(
    private val currentUserUidProvider: CurrentUserUidProvider
) : MedicationReminderReadStateRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun observeReadState(patientUid: String): Observable<MedicationReminderReadState> {
        val caregiverUid = currentUserUidProvider.requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(caregiverUid)
            .collection(FirestorePaths.MEDICATION_REMINDER_READ_STATES)
            .document(patientUid)

        return Observable.create { emitter ->
            val registration = doc.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!emitter.isDisposed) emitter.onError(error)
                    return@addSnapshotListener
                }
                val rawIds = snapshot?.get("readEventIds") as? List<*>
                val ids = rawIds?.mapNotNull { it as? String }?.toSet().orEmpty()
                if (!emitter.isDisposed) {
                    emitter.onNext(MedicationReminderReadState(patientUid, ids))
                }
            }
            emitter.setCancellable { registration.remove() }
        }
    }

    override fun markRead(patientUid: String, eventId: String): Completable {
        val caregiverUid = currentUserUidProvider.requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(caregiverUid)
            .collection(FirestorePaths.MEDICATION_REMINDER_READ_STATES)
            .document(patientUid)

        val payload = mapOf(
            "patientUid" to patientUid,
            "readEventIds" to FieldValue.arrayUnion(eventId),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return doc.set(payload, SetOptions.merge()).toCompletable()
    }

    override fun setReadEvents(patientUid: String, eventIds: List<String>): Completable {
        val caregiverUid = currentUserUidProvider.requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(caregiverUid)
            .collection(FirestorePaths.MEDICATION_REMINDER_READ_STATES)
            .document(patientUid)

        val payload = mapOf(
            "patientUid" to patientUid,
            "readEventIds" to eventIds.distinct(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return doc.set(payload, SetOptions.merge()).toCompletable()
    }
}

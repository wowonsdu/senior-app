package zdrowy.senior.io.data.settings.reminders

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.data.firestore.toCompletable
import zdrowy.senior.io.domain.settings.reminders.MedicationReminderEvent
import zdrowy.senior.io.domain.settings.reminders.MedicationReminderEventRepository

class FirestoreMedicationReminderEventRepository : MedicationReminderEventRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun upsertEvent(patientUid: String, event: MedicationReminderEvent): Completable {
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(patientUid)
            .collection(FirestorePaths.MEDICATION_REMINDER_EVENTS)
            .document(event.id)

        val payload = mapOf(
            "patientUid" to event.patientUid,
            "medicationId" to event.medicationId,
            "scheduledAtMs" to event.scheduledAtMs,
            "medicationName" to event.medicationName,
            "dosage" to event.dosage,
            "scheduleTime" to event.scheduleTime,
            "updatedAt" to FieldValue.serverTimestamp(),
            "createdAt" to FieldValue.serverTimestamp()
        )

        return doc.set(payload, SetOptions.merge()).toCompletable()
    }

    override fun observeRecentEvents(
        patientUid: String,
        limit: Int
    ): Observable<List<MedicationReminderEvent>> {
        return Observable.create { emitter ->
            val query = firestore.collection(FirestorePaths.USERS)
                .document(patientUid)
                .collection(FirestorePaths.MEDICATION_REMINDER_EVENTS)
                .orderBy("scheduledAtMs", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            val registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!emitter.isDisposed) emitter.onError(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().map { doc ->
                    MedicationReminderEvent(
                        id = doc.id,
                        patientUid = doc.getString("patientUid").orEmpty(),
                        medicationId = doc.getString("medicationId").orEmpty(),
                        scheduledAtMs = doc.getLong("scheduledAtMs") ?: 0L,
                        medicationName = doc.getString("medicationName").orEmpty(),
                        dosage = doc.getString("dosage").orEmpty(),
                        scheduleTime = doc.getString("scheduleTime").orEmpty()
                    )
                }
                if (!emitter.isDisposed) emitter.onNext(items)
            }
            emitter.setCancellable { registration.remove() }
        }
    }
}

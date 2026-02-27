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
import zdrowy.senior.io.domain.settings.reminders.MedicationReminderTakenSource

class FirestoreMedicationReminderEventRepository : MedicationReminderEventRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun upsertEvent(patientUid: String, event: MedicationReminderEvent): Completable {
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(patientUid)
            .collection(FirestorePaths.MEDICATION_REMINDER_EVENTS)
            .document(event.id)

        val payload = mutableMapOf<String, Any?>(
            "patientUid" to event.patientUid,
            "medicationId" to event.medicationId,
            "scheduledAtMs" to event.scheduledAtMs,
            "medicationName" to event.medicationName,
            "dosage" to event.dosage,
            "scheduleTime" to event.scheduleTime,
            "updatedAt" to FieldValue.serverTimestamp(),
            "createdAt" to FieldValue.serverTimestamp()
        )

        return firestore.runTransaction { tx ->
            val snapshot = tx.get(doc)
            if (!snapshot.exists()) {
                payload["isTaken"] = false
                payload["takenAtMs"] = null
                payload["takenSource"] = null
            }
            tx.set(doc, payload, SetOptions.merge())
            true
        }.toCompletable()
    }

    override fun confirmTaken(
        patientUid: String,
        event: MedicationReminderEvent,
        confirmedAtMs: Long,
        source: MedicationReminderTakenSource
    ): Completable {
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(patientUid)
            .collection(FirestorePaths.MEDICATION_REMINDER_EVENTS)
            .document(event.id)

        return firestore.runTransaction { tx ->
            val snapshot = tx.get(doc)
            val alreadyTaken = snapshot.getBoolean("isTaken") == true ||
                ((snapshot.get("takenAtMs") as? Number)?.toLong() != null)
            if (alreadyTaken) {
                return@runTransaction true
            }

            val basePayload = mutableMapOf<String, Any?>(
                "patientUid" to event.patientUid,
                "medicationId" to event.medicationId,
                "scheduledAtMs" to event.scheduledAtMs,
                "medicationName" to event.medicationName,
                "dosage" to event.dosage,
                "scheduleTime" to event.scheduleTime,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!snapshot.exists()) {
                basePayload["createdAt"] = FieldValue.serverTimestamp()
            }

            val confirmPayload = mapOf(
                "isTaken" to true,
                "takenAtMs" to confirmedAtMs,
                "takenSource" to source.name,
                "takenUpdatedAt" to FieldValue.serverTimestamp()
            )

            tx.set(doc, basePayload, SetOptions.merge())
            tx.set(doc, confirmPayload, SetOptions.merge())
            true
        }.toCompletable()
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
                        scheduleTime = doc.getString("scheduleTime").orEmpty(),
                        isTaken = doc.getBoolean("isTaken") ?: false,
                        takenAtMs = (doc.get("takenAtMs") as? Number)?.toLong(),
                        takenSource = doc.getString("takenSource")
                            ?.let { raw -> runCatching { MedicationReminderTakenSource.valueOf(raw) }.getOrNull() }
                    )
                }
                if (!emitter.isDisposed) emitter.onNext(items)
            }
            emitter.setCancellable { registration.remove() }
        }
    }
}

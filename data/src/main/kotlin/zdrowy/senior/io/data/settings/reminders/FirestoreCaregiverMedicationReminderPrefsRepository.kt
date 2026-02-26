package zdrowy.senior.io.data.settings.reminders

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.data.firestore.toCompletable
import zdrowy.senior.io.domain.settings.reminders.CaregiverMedicationReminderPrefsRepository
import zdrowy.senior.io.domain.user.CurrentUserUidProvider

class FirestoreCaregiverMedicationReminderPrefsRepository(
    private val currentUserUidProvider: CurrentUserUidProvider
) : CaregiverMedicationReminderPrefsRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun observePrefs(): Observable<Map<String, Boolean>> {
        val caregiverUid = currentUserUidProvider.requireUid()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(caregiverUid)
            .collection(FirestorePaths.SETTINGS)
            .document(FirestorePaths.MEDICATION_REMINDER_PREFS)
            .collection("items")

        return Observable.create { emitter ->
            val registration = col.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!emitter.isDisposed) emitter.onError(error)
                    return@addSnapshotListener
                }
                val map = snapshot?.documents.orEmpty().associate { doc ->
                    val enabled = doc.getBoolean("enabled") ?: true
                    doc.id to enabled
                }
                if (!emitter.isDisposed) emitter.onNext(map)
            }
            emitter.setCancellable { registration.remove() }
        }
    }

    override fun setEnabled(patientUid: String, enabled: Boolean): Completable {
        val caregiverUid = currentUserUidProvider.requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(caregiverUid)
            .collection(FirestorePaths.SETTINGS)
            .document(FirestorePaths.MEDICATION_REMINDER_PREFS)
            .collection("items")
            .document(patientUid)

        val payload = mapOf(
            "patientUid" to patientUid,
            "enabled" to enabled,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return doc.set(payload, SetOptions.merge()).toCompletable()
    }
}

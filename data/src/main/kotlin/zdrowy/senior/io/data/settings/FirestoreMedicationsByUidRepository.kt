package zdrowy.senior.io.data.settings

import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.rxjava3.core.Observable
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.domain.settings.Medication
import zdrowy.senior.io.domain.settings.MedicationByUidRepository

class FirestoreMedicationsByUidRepository : MedicationByUidRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun observeMedications(patientUid: String): Observable<List<Medication>> {
        return Observable.create { emitter ->
            val col = firestore.collection(FirestorePaths.USERS)
                .document(patientUid)
                .collection(FirestorePaths.MEDICATIONS)

            val registration = col.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!emitter.isDisposed) emitter.onError(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().map { doc ->
                    Medication(
                        id = doc.id,
                        name = doc.getString("name").orEmpty(),
                        dosage = doc.getString("dosage").orEmpty(),
                        schedule = doc.getString("schedule").orEmpty(),
                        notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: false
                    )
                }.sortedBy { it.name.lowercase() }
                if (!emitter.isDisposed) emitter.onNext(items)
            }
            emitter.setCancellable { registration.remove() }
        }
    }
}

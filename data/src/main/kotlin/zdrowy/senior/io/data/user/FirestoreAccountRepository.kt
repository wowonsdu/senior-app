package zdrowy.senior.io.data.user

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.data.firestore.toCompletable
import zdrowy.senior.io.domain.user.AccountRepository
import io.reactivex.rxjava3.core.Completable

class FirestoreAccountRepository : AccountRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun deleteCurrentUserAccount(): Completable {
        val user = auth.currentUser ?: return Completable.error(IllegalStateException("Not authenticated"))
        val uid = user.uid
        return deleteUserData(uid)
            .andThen(user.delete().toCompletable())
    }

    override fun deleteUserData(uid: String): Completable {
        if (uid.isBlank()) return Completable.error(IllegalStateException("Brak identyfikatora uzytkownika"))
        return Completable.fromAction {
            val userDoc = firestore.collection(FirestorePaths.USERS).document(uid)

            deleteCollection(userDoc.collection(FirestorePaths.DISEASES))
            deleteCollection(userDoc.collection(FirestorePaths.MEDICATIONS))
            deleteCollection(userDoc.collection(FirestorePaths.MEDICATION_REMINDER_EVENTS))
            deleteCollection(userDoc.collection(FirestorePaths.MEDICATION_REMINDER_READ_STATES))
            deleteCollection(userDoc.collection(FirestorePaths.MEASUREMENTS))
            deleteCollection(userDoc.collection(FirestorePaths.MEASUREMENT_READ_STATES))
            deleteCollection(userDoc.collection(FirestorePaths.CONTACTS))
            deleteCollection(userDoc.collection(FirestorePaths.ALERT_EVENTS))
            deleteCollection(
                userDoc.collection(FirestorePaths.SETTINGS)
                    .document(FirestorePaths.MEDICATION_REMINDER_PREFS)
                    .collection("items")
            )

            Tasks.await(
                userDoc.collection(FirestorePaths.SETTINGS)
                    .document(FirestorePaths.PERSONAL_DATA)
                    .delete()
            )
            Tasks.await(
                userDoc.collection(FirestorePaths.SETTINGS)
                    .document(FirestorePaths.NOTIFICATIONS)
                    .delete()
            )

            Tasks.await(userDoc.delete())
            deleteCareLinksForUser(uid)
        }
    }

    private fun deleteCareLinksForUser(uid: String) {
        deleteCareLinksByField("patientUid", uid)
        deleteCareLinksByField("caregiverUid", uid)
    }

    private fun deleteCareLinksByField(field: String, uid: String) {
        val query = firestore.collection(FirestorePaths.CARE_LINKS)
            .whereEqualTo(field, uid)
        val snapshot = Tasks.await(query.get())
        if (snapshot.isEmpty) return
        val batch = firestore.batch()
        snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
        Tasks.await(batch.commit())
    }

    private fun deleteCollection(collection: CollectionReference, batchSize: Int = 100) {
        var snapshot = Tasks.await(collection.limit(batchSize.toLong()).get())
        while (!snapshot.isEmpty) {
            val batch = firestore.batch()
            snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
            Tasks.await(batch.commit())
            if (snapshot.size() < batchSize) break
            snapshot = Tasks.await(collection.limit(batchSize.toLong()).get())
        }
    }
}

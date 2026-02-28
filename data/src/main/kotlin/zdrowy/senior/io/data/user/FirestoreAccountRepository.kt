package zdrowy.senior.io.data.user

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.data.firestore.toCompletable
import zdrowy.senior.io.domain.user.AccountRepository
import zdrowy.senior.io.domain.user.UserRole
import io.reactivex.rxjava3.core.Completable

class FirestoreAccountRepository : AccountRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun deleteCurrentUserAccount(): Completable {
        val user = auth.currentUser ?: return Completable.error(IllegalStateException("Not authenticated"))
        val uid = user.uid
        return Completable.fromAction {
            val role = readUserRole(uid)
            if (role == UserRole.PATIENT) {
                detachOrDeletePatientProfile(uid)
            } else {
                deleteUserDataBlocking(uid)
            }
        }
            .andThen(user.delete().toCompletable())
    }

    override fun deleteUserData(uid: String): Completable {
        if (uid.isBlank()) return Completable.error(IllegalStateException("Brak identyfikatora uzytkownika"))
        return Completable.fromAction { deleteUserDataBlocking(uid) }
    }

    private fun detachOrDeletePatientProfile(accountUid: String) {
        val linkedPatientUid = readLinkedPatientUid(accountUid)
        val profileUid = linkedPatientUid.ifBlank { accountUid }
        deletePatientAccountLinkByAccountUid(accountUid)

        val profileHasCaregivers = hasAnyCaregiverForPatient(profileUid)
        if (profileUid != accountUid) {
            deleteUserDataBlocking(accountUid)
            if (!profileHasCaregivers) {
                deleteUserDataBlocking(profileUid)
            }
            return
        }
        if (!profileHasCaregivers) {
            deleteUserDataBlocking(accountUid)
        }
    }

    private fun readUserRole(uid: String): UserRole? {
        val snapshot = Tasks.await(
            firestore.collection(FirestorePaths.USERS)
                .document(uid)
                .get()
        )
        val rawRole = snapshot.getString("role").orEmpty()
        return runCatching { UserRole.valueOf(rawRole) }.getOrNull()
    }

    private fun readLinkedPatientUid(accountUid: String): String {
        val snapshot = Tasks.await(
            firestore.collection(FirestorePaths.PATIENT_ACCOUNT_LINKS)
                .document(accountUid)
                .get()
        )
        return snapshot.getString("patientUid").orEmpty().trim()
    }

    private fun hasAnyCaregiverForPatient(patientUid: String): Boolean {
        val snapshot = Tasks.await(
            firestore.collection(FirestorePaths.CARE_LINKS)
                .whereEqualTo("patientUid", patientUid)
                .limit(1)
                .get()
        )
        return !snapshot.isEmpty
    }

    private fun deleteUserDataBlocking(uid: String) {
        val cleanedUid = uid.trim()
        if (cleanedUid.isBlank()) return
        val userDoc = firestore.collection(FirestorePaths.USERS).document(cleanedUid)

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
        deleteCareLinksForUser(cleanedUid)
        deletePatientAccountLinksByPatientUid(cleanedUid)
        deletePatientAccountLinkByAccountUid(cleanedUid)
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

    private fun deletePatientAccountLinksByPatientUid(patientUid: String) {
        val snapshot = Tasks.await(
            firestore.collection(FirestorePaths.PATIENT_ACCOUNT_LINKS)
                .whereEqualTo("patientUid", patientUid)
                .get()
        )
        if (snapshot.isEmpty) return
        val batch = firestore.batch()
        snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
        Tasks.await(batch.commit())
    }

    private fun deletePatientAccountLinkByAccountUid(accountUid: String) {
        val doc = firestore.collection(FirestorePaths.PATIENT_ACCOUNT_LINKS).document(accountUid)
        Tasks.await(doc.delete())
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

package zdrowy.senior.io.data.user

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.data.firestore.toSingle
import zdrowy.senior.io.domain.user.PatientAccountLinkRepository

class FirestorePatientAccountLinkRepository : PatientAccountLinkRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun observeLinkedPatientUid(accountUid: String): Observable<String?> {
        if (accountUid.isBlank()) return Observable.error(IllegalStateException("Brak UID konta"))
        return Observable.create { emitter ->
            val doc = firestore.collection(FirestorePaths.PATIENT_ACCOUNT_LINKS).document(accountUid)
            val registration = doc.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!emitter.isDisposed) emitter.onError(error)
                    return@addSnapshotListener
                }
                val patientUid = snapshot?.getString("patientUid")?.trim().orEmpty()
                if (!emitter.isDisposed) {
                    emitter.onNext(patientUid.ifBlank { null })
                }
            }
            emitter.setCancellable { registration.remove() }
        }
    }

    override fun getLinkedPatientUid(accountUid: String): Single<String?> {
        if (accountUid.isBlank()) return Single.error(IllegalStateException("Brak UID konta"))
        val doc = firestore.collection(FirestorePaths.PATIENT_ACCOUNT_LINKS).document(accountUid)
        return doc.get()
            .toSingle()
            .map { snapshot ->
                val patientUid = snapshot.getString("patientUid")?.trim().orEmpty()
                patientUid.ifBlank { null }
            }
    }

    override fun upsertPatientLink(accountUid: String, patientUid: String, linkCode: String?): Completable {
        if (accountUid.isBlank()) return Completable.error(IllegalStateException("Brak UID konta"))
        val cleanedPatientUid = patientUid.trim()
        if (cleanedPatientUid.isBlank()) return Completable.error(IllegalStateException("Brak UID pacjenta"))
        val doc = firestore.collection(FirestorePaths.PATIENT_ACCOUNT_LINKS).document(accountUid)
        val payload = mutableMapOf<String, Any>(
            "accountUid" to accountUid,
            "patientUid" to cleanedPatientUid,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!linkCode.isNullOrBlank()) payload["linkedByCode"] = linkCode.trim()
        return Completable.fromAction {
            Tasks.await(doc.set(payload, SetOptions.merge()))
        }
    }

    override fun removePatientLink(accountUid: String): Completable {
        if (accountUid.isBlank()) return Completable.complete()
        val doc = firestore.collection(FirestorePaths.PATIENT_ACCOUNT_LINKS).document(accountUid)
        return Completable.fromAction {
            Tasks.await(doc.delete())
        }
    }

    override fun removeLinksByPatientUid(patientUid: String): Completable {
        val cleanedPatientUid = patientUid.trim()
        if (cleanedPatientUid.isBlank()) return Completable.complete()
        return Completable.fromAction {
            val snapshot = Tasks.await(
                firestore.collection(FirestorePaths.PATIENT_ACCOUNT_LINKS)
                    .whereEqualTo("patientUid", cleanedPatientUid)
                    .get()
            )
            if (snapshot.isEmpty) return@fromAction
            val batch = firestore.batch()
            snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
            Tasks.await(batch.commit())
        }
    }
}

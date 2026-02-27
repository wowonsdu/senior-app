package zdrowy.senior.io.data.visit

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.data.firestore.toCompletable
import zdrowy.senior.io.data.firestore.toSingle
import zdrowy.senior.io.domain.user.CurrentUserUidProvider
import zdrowy.senior.io.domain.visit.Visit
import zdrowy.senior.io.domain.visit.VisitDraft
import zdrowy.senior.io.domain.visit.VisitRepository
import zdrowy.senior.io.domain.visit.VisitUpdate

class FirestoreVisitRepository(
    private val currentUserUidProvider: CurrentUserUidProvider
) : VisitRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun observeVisits(): Observable<List<Visit>> {
        val caregiverUid = currentUserUidProvider.requireUid()
        val query = firestore.collection(FirestorePaths.USERS)
            .document(caregiverUid)
            .collection(FirestorePaths.VISITS)
            .orderBy("scheduledAtMs", Query.Direction.ASCENDING)

        return Observable.create { emitter ->
            val registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!emitter.isDisposed) emitter.onError(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().map { doc ->
                    Visit(
                        id = doc.id,
                        patientUid = doc.getString("patientUid").orEmpty(),
                        title = doc.getString("title").orEmpty(),
                        scheduledAtMs = (doc.get("scheduledAtMs") as? Number)?.toLong() ?: 0L,
                        location = doc.getString("location").orEmpty(),
                        notes = doc.getString("notes").orEmpty(),
                        isCompletedManual = doc.getBoolean("isCompletedManual") ?: false,
                        completedAtMs = (doc.get("completedAtMs") as? Number)?.toLong(),
                        reminderEnabled = doc.getBoolean("reminderEnabled") ?: false,
                        reminderOffsetMinutes = (doc.get("reminderOffsetMinutes") as? Number)?.toInt(),
                        createdAtMs = (doc.get("createdAtMs") as? Number)?.toLong() ?: 0L,
                        updatedAtMs = (doc.get("updatedAtMs") as? Number)?.toLong() ?: 0L
                    )
                }
                if (!emitter.isDisposed) emitter.onNext(items)
            }
            emitter.setCancellable { registration.remove() }
        }
    }

    override fun addVisit(draft: VisitDraft): Single<String> {
        val caregiverUid = currentUserUidProvider.requireUid()
        val nowMs = System.currentTimeMillis()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(caregiverUid)
            .collection(FirestorePaths.VISITS)

        val payload = mutableMapOf<String, Any>(
            "patientUid" to draft.patientUid,
            "title" to draft.title,
            "scheduledAtMs" to draft.scheduledAtMs,
            "location" to draft.location,
            "notes" to draft.notes,
            "isCompletedManual" to false,
            "reminderEnabled" to draft.reminderEnabled,
            "createdAtMs" to nowMs,
            "updatedAtMs" to nowMs,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (draft.reminderEnabled) {
            payload["reminderOffsetMinutes"] = draft.reminderOffsetMinutes ?: 0
        }

        return col.add(payload)
            .toSingle()
            .map { it.id }
    }

    override fun updateVisit(visitId: String, update: VisitUpdate): Completable {
        val caregiverUid = currentUserUidProvider.requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(caregiverUid)
            .collection(FirestorePaths.VISITS)
            .document(visitId)

        val payload = mutableMapOf<String, Any>(
            "updatedAtMs" to System.currentTimeMillis(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        update.patientUid?.let { payload["patientUid"] = it }
        update.title?.let { payload["title"] = it }
        update.scheduledAtMs?.let { payload["scheduledAtMs"] = it }
        update.location?.let { payload["location"] = it }
        update.notes?.let { payload["notes"] = it }
        update.reminderEnabled?.let { enabled ->
            payload["reminderEnabled"] = enabled
            if (!enabled) payload["reminderOffsetMinutes"] = FieldValue.delete()
        }
        val reminderOffsetMinutes = update.reminderOffsetMinutes
        if (reminderOffsetMinutes != null) {
            payload["reminderOffsetMinutes"] = reminderOffsetMinutes
        }
        update.isCompletedManual?.let { payload["isCompletedManual"] = it }
        update.completedAtMs?.let { payload["completedAtMs"] = it }

        return doc.update(payload as Map<String, Any>).toCompletable()
    }

    override fun removeVisit(visitId: String): Completable {
        val caregiverUid = currentUserUidProvider.requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(caregiverUid)
            .collection(FirestorePaths.VISITS)
            .document(visitId)
        return doc.delete().toCompletable()
    }

    override fun markVisitCompleted(visitId: String, completedAtMs: Long): Completable {
        val caregiverUid = currentUserUidProvider.requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(caregiverUid)
            .collection(FirestorePaths.VISITS)
            .document(visitId)

        return doc.update(
            mapOf(
                "isCompletedManual" to true,
                "completedAtMs" to completedAtMs,
                "updatedAtMs" to System.currentTimeMillis(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).toCompletable()
    }
}

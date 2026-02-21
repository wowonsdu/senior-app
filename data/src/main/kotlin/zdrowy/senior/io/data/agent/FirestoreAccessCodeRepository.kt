package zdrowy.senior.io.data.agent

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.data.firestore.PatientUidProvider
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.data.firestore.toSingle
import zdrowy.senior.io.domain.agent.AccessCode
import zdrowy.senior.io.domain.agent.AccessCodeRepository
import kotlin.random.Random

class FirestoreAccessCodeRepository(
    private val uidProvider: PatientUidProvider
) : AccessCodeRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val random: Random = Random(System.currentTimeMillis())

    override fun generateAccessCodeForAgent(agentId: String, ttlSeconds: Long): Single<AccessCode> {
        val uid = requireUid()
        val expiresAtMs = System.currentTimeMillis() + ttlSeconds * 1000
        return createUniqueCode(
            remainingAttempts = 8,
            uid = uid,
            contactId = agentId,
            expiresAtMs = expiresAtMs
        )
    }

    private fun createUniqueCode(
        remainingAttempts: Int,
        uid: String,
        contactId: String,
        expiresAtMs: Long
    ): Single<AccessCode> {
        if (remainingAttempts <= 0) {
            return Single.error(IllegalStateException("Unable to generate unique access code"))
        }

        val code = random.nextInt(100000, 1000000).toString()
        val accessCode = AccessCode(code = code, expiresAt = expiresAtMs)
        val doc = firestore.collection(FirestorePaths.ACCESS_CODES).document(code)

        return firestore.runTransaction { tx ->
            val snapshot = tx.get(doc)
            if (snapshot.exists()) throw CodeCollisionException()
            tx.set(
                doc,
                mapOf(
                    "patientUid" to uid,
                    "contactId" to contactId,
                    "expiresAtMs" to expiresAtMs,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "consumedAt" to null,
                    "consumedByUid" to null
                )
            )
            true
        }
            .toSingle()
            .map { accessCode }
            .onErrorResumeNext { error ->
                if (error is CodeCollisionException) {
                    createUniqueCode(
                        remainingAttempts = remainingAttempts - 1,
                        uid = uid,
                        contactId = contactId,
                        expiresAtMs = expiresAtMs
                    )
                } else {
                    Single.error(error)
                }
            }
    }

    private fun requireUid(): String = uidProvider.requirePatientUid()

    private class CodeCollisionException : RuntimeException()
}

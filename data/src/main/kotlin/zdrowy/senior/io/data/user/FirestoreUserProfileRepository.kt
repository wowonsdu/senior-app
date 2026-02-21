package zdrowy.senior.io.data.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.data.firestore.toCompletable
import zdrowy.senior.io.data.firestore.toSingle
import zdrowy.senior.io.domain.user.UserProfileRepository
import zdrowy.senior.io.domain.user.UserRole

class FirestoreUserProfileRepository : UserProfileRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun ensureCurrentUserProfile(role: UserRole): Completable {
        val user = auth.currentUser ?: return Completable.error(IllegalStateException("Not authenticated"))
        val uid = user.uid
        val doc = firestore.collection("users").document(uid)

        return firestore.runTransaction { tx ->
            val snapshot = tx.get(doc)
            val now = FieldValue.serverTimestamp()
            val phoneNumberE164 = user.phoneNumber

            if (!snapshot.exists()) {
                tx.set(
                    doc,
                    mapOf(
                        "role" to role.name,
                        "schemaVersion" to 1,
                        "createdAt" to now,
                        "lastLoginAt" to now,
                        "phoneNumberE164" to phoneNumberE164
                    )
                )
            } else {
                val existingRole = snapshot.getString("role")
                if (existingRole != null && existingRole != role.name) {
                    throw IllegalStateException("Account role mismatch (expected ${role.name}, got $existingRole)")
                }
                tx.update(
                    doc,
                    mapOf(
                        "lastLoginAt" to now,
                        "phoneNumberE164" to phoneNumberE164
                    )
                )
            }
            null
        }.toCompletable()
    }

    override fun getCurrentUserRole(): Single<UserRole> {
        val user = auth.currentUser ?: return Single.error(IllegalStateException("Not authenticated"))
        val doc = firestore.collection("users").document(user.uid)
        return doc.get()
            .toSingle()
            .map { snapshot ->
                val raw = snapshot.getString("role") ?: throw IllegalStateException("User role missing")
                runCatching { UserRole.valueOf(raw) }
                    .getOrElse { throw IllegalStateException("Invalid role: $raw") }
            }
    }
}

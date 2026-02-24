package zdrowy.senior.io.data.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.data.firestore.toCompletable
import zdrowy.senior.io.data.firestore.toSingle
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.domain.alert.AlertChannel
import zdrowy.senior.io.domain.measurement.MeasurementType
import zdrowy.senior.io.domain.user.UserProfileRepository
import zdrowy.senior.io.domain.user.UserRole

class FirestoreUserProfileRepository : UserProfileRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val defaultPressureSystolicMin = 90.0
    private val defaultPressureSystolicMax = 129.0
    private val defaultPressureDiastolicMin = 60.0
    private val defaultPressureDiastolicMax = 84.0
    private val defaultSugarMin = 70.0
    private val defaultSugarMax = 125.0
    private val defaultPulseMin = 60.0
    private val defaultPulseMax = 100.0

    override fun ensureCurrentUserProfile(role: UserRole): Completable {
        val user = auth.currentUser ?: return Completable.error(IllegalStateException("Not authenticated"))
        val uid = user.uid
        val doc = firestore.collection(FirestorePaths.USERS).document(uid)

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
                val settingsCol = doc.collection(FirestorePaths.SETTINGS)
                if (role == UserRole.PATIENT || role == UserRole.CAREGIVER) {
                    val personalDoc = settingsCol.document(FirestorePaths.PERSONAL_DATA)
                    tx.set(personalDoc, defaultPersonalDataPayload())
                }
                if (role == UserRole.PATIENT) {
                    val notificationsDoc = settingsCol.document(FirestorePaths.NOTIFICATIONS)
                    tx.set(
                        notificationsDoc,
                        mapOf(
                            "alerts" to buildDefaultAlertsMap(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
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
        val doc = firestore.collection(FirestorePaths.USERS).document(user.uid)
        return doc.get()
            .toSingle()
            .map { snapshot ->
                val raw = snapshot.getString("role") ?: throw IllegalStateException("User role missing")
                runCatching { UserRole.valueOf(raw) }
                    .getOrElse { throw IllegalStateException("Invalid role: $raw") }
            }
    }

    private fun defaultPersonalDataPayload(): Map<String, Any?> {
        return mapOf(
            "firstName" to "",
            "lastName" to "",
            "pesel" to "",
            "phoneNumber" to "",
            "email" to "",
            "address" to "",
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    private fun buildDefaultAlertsMap(): Map<String, Any?> {
        return MeasurementType.values().associate { type ->
            type.name to buildDefaultAlertMap(type)
        }
    }

    private fun buildDefaultAlertMap(type: MeasurementType): Map<String, Any?> {
        val payload = mutableMapOf<String, Any?>(
            "enabled" to true,
            "spikePercent" to 20,
            "windowCount" to 3,
            "channels" to listOf(AlertChannel.APP.name),
            "caregiverUids" to emptyList<String>(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        when (type) {
            MeasurementType.PRESSURE -> {
                payload["systolicMin"] = defaultPressureSystolicMin
                payload["systolicMax"] = defaultPressureSystolicMax
                payload["diastolicMin"] = defaultPressureDiastolicMin
                payload["diastolicMax"] = defaultPressureDiastolicMax
            }
            MeasurementType.SUGAR -> {
                payload["min"] = defaultSugarMin
                payload["max"] = defaultSugarMax
                payload["dropDelta"] = null
                payload["dropWindowMinutes"] = null
            }
            MeasurementType.PULSE -> {
                payload["min"] = defaultPulseMin
                payload["max"] = defaultPulseMax
            }
            MeasurementType.INSULIN -> {
                payload["min"] = null
                payload["max"] = null
            }
        }
        return payload
    }
}

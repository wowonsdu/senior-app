package zdrowy.senior.io.data.alert

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.data.firestore.toCompletable
import zdrowy.senior.io.data.firestore.toSingle
import zdrowy.senior.io.domain.alert.AlertChannel
import zdrowy.senior.io.domain.alert.AlertConfig
import zdrowy.senior.io.domain.alert.AlertRepository
import zdrowy.senior.io.domain.alert.AlertSetting
import zdrowy.senior.io.domain.measurement.MeasurementType

class FirestoreAlertRepository : AlertRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun getAlertConfig(): Single<AlertConfig> {
        val uid = requireUid()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.ALERTS)

        return col.get()
            .toSingle()
            .map { snapshot ->
                val byType = snapshot.documents.associateBy { it.id }
                val settings = MeasurementType.values().map { type ->
                    val doc = byType[type.name]
                    val enabled = doc?.getBoolean("enabled") ?: true
                    val min = (doc?.get("min") as? Number)?.toDouble()
                    val max = (doc?.get("max") as? Number)?.toDouble()
                    val spikePercent = ((doc?.get("spikePercent") as? Number)?.toInt()) ?: 20
                    val windowCount = ((doc?.get("windowCount") as? Number)?.toInt()) ?: 3
                    val channels = parseChannels(doc?.get("channels"))
                    val caregiverUids = parseStringList(doc?.get("caregiverUids"))

                    AlertSetting(
                        type = type,
                        enabled = enabled,
                        min = min,
                        max = max,
                        spikePercent = spikePercent,
                        windowCount = windowCount,
                        channels = if (channels.isEmpty()) setOf(AlertChannel.APP) else channels,
                        caregiverIds = caregiverUids
                    )
                }
                AlertConfig(settings = settings)
            }
    }

    override fun updateAlertConfig(config: AlertConfig): Completable {
        val uid = requireUid()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.ALERTS)

        val batch = firestore.batch()
        config.settings.forEach { setting ->
            val doc = col.document(setting.type.name)
            batch.set(
                doc,
                mapOf(
                    "enabled" to setting.enabled,
                    "min" to setting.min,
                    "max" to setting.max,
                    "spikePercent" to setting.spikePercent,
                    "windowCount" to setting.windowCount,
                    "channels" to setting.channels.map { it.name },
                    "caregiverUids" to setting.caregiverIds,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
        }
        return batch.commit().toCompletable()
    }

    override fun setAlertEnabled(type: MeasurementType, enabled: Boolean): Completable {
        return setFields(type, mapOf("enabled" to enabled))
    }

    override fun updateCriticalThresholds(type: MeasurementType, min: Double?, max: Double?): Completable {
        return setFields(type, mapOf("min" to min, "max" to max))
    }

    override fun updateSpikeRules(type: MeasurementType, percent: Int, windowCount: Int): Completable {
        return setFields(type, mapOf("spikePercent" to percent, "windowCount" to windowCount))
    }

    override fun updateAlertChannels(type: MeasurementType, channels: Set<AlertChannel>): Completable {
        return setFields(type, mapOf("channels" to channels.map { it.name }))
    }

    override fun updateAlertCaregivers(type: MeasurementType, caregiverIds: List<String>): Completable {
        return setFields(type, mapOf("caregiverUids" to caregiverIds))
    }

    private fun setFields(type: MeasurementType, fields: Map<String, Any?>): Completable {
        val uid = requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.ALERTS)
            .document(type.name)

        val payload = fields.toMutableMap()
        payload["updatedAt"] = FieldValue.serverTimestamp()

        return doc.set(payload, SetOptions.merge()).toCompletable()
    }

    private fun parseChannels(raw: Any?): Set<AlertChannel> {
        val list = (raw as? List<*>)?.mapNotNull { it as? String }.orEmpty()
        return list.mapNotNull { name ->
            runCatching { AlertChannel.valueOf(name) }.getOrNull()
        }.toSet()
    }

    private fun parseStringList(raw: Any?): List<String> {
        return (raw as? List<*>)?.mapNotNull { it as? String }.orEmpty()
    }

    private fun requireUid(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")
    }
}


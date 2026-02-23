package zdrowy.senior.io.data.alert

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.data.firestore.PatientUidProvider
import zdrowy.senior.io.data.firestore.toCompletable
import zdrowy.senior.io.data.firestore.toSingle
import zdrowy.senior.io.domain.alert.AlertChannel
import zdrowy.senior.io.domain.alert.AlertConfig
import zdrowy.senior.io.domain.alert.AlertRepository
import zdrowy.senior.io.domain.alert.AlertSetting
import zdrowy.senior.io.domain.measurement.BloodPressureSeverity
import zdrowy.senior.io.domain.measurement.BloodPressureStandard
import zdrowy.senior.io.domain.measurement.MeasurementType

class FirestoreAlertRepository(
    private val uidProvider: PatientUidProvider
) : AlertRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun getAlertConfig(): Single<AlertConfig> {
        val uid = requireUid()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.ALERTS)

        return col.get()
            .toSingle()
            .map { snapshot -> buildAlertConfig(snapshot.documents) }
    }

    override fun observeAlertConfig(): Observable<AlertConfig> {
        return Observable.create { emitter ->
            val uid = try {
                requireUid()
            } catch (error: Throwable) {
                emitter.onError(error)
                return@create
            }
            val col = firestore.collection(FirestorePaths.USERS)
                .document(uid)
                .collection(FirestorePaths.ALERTS)

            val registration = col.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!emitter.isDisposed) emitter.onError(error)
                    return@addSnapshotListener
                }
                val documents = snapshot?.documents.orEmpty()
                if (!emitter.isDisposed) emitter.onNext(buildAlertConfig(documents))
            }
            emitter.setCancellable { registration.remove() }
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
            val isPressure = setting.type == MeasurementType.PRESSURE
            batch.set(
                doc,
                mapOf(
                    "enabled" to setting.enabled,
                    "min" to (if (isPressure) (setting.systolicMin ?: setting.min) else setting.min),
                    "max" to (if (isPressure) (setting.systolicMax ?: setting.max) else setting.max),
                    "systolicMin" to (if (isPressure) (setting.systolicMin ?: setting.min) else null),
                    "systolicMax" to (if (isPressure) (setting.systolicMax ?: setting.max) else null),
                    "diastolicMin" to (if (isPressure) setting.diastolicMin else null),
                    "diastolicMax" to (if (isPressure) setting.diastolicMax else null),
                    "categoryEnabled" to (if (isPressure) setting.categoryEnabled else null),
                    "categoryThreshold" to (if (isPressure) setting.categoryThreshold.name else null),
                    "categoryCooldownMinutes" to (if (isPressure) setting.categoryCooldownMinutes else null),
                    "bpStandard" to (if (isPressure) setting.bpStandard.name else null),
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

    override fun updateBloodPressureCriticalThresholds(
        systolicMin: Double?,
        systolicMax: Double?,
        diastolicMin: Double?,
        diastolicMax: Double?
    ): Completable {
        return setFields(
            MeasurementType.PRESSURE,
            mapOf(
                "min" to systolicMin,
                "max" to systolicMax,
                "systolicMin" to systolicMin,
                "systolicMax" to systolicMax,
                "diastolicMin" to diastolicMin,
                "diastolicMax" to diastolicMax
            )
        )
    }

    override fun updateBloodPressureCategoryRules(
        enabled: Boolean,
        threshold: BloodPressureSeverity,
        cooldownMinutes: Int,
        standard: BloodPressureStandard
    ): Completable {
        return setFields(
            MeasurementType.PRESSURE,
            mapOf(
                "categoryEnabled" to enabled,
                "categoryThreshold" to threshold.name,
                "categoryCooldownMinutes" to cooldownMinutes,
                "bpStandard" to standard.name
            )
        )
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

    private fun parseDouble(raw: Any?): Double? = (raw as? Number)?.toDouble()

    private fun parseInt(raw: Any?): Int? = (raw as? Number)?.toInt()

    private fun parseBpSeverity(raw: Any?): BloodPressureSeverity? {
        val name = raw as? String ?: return null
        return runCatching { BloodPressureSeverity.valueOf(name) }.getOrNull()
    }

    private fun parseBpStandard(raw: Any?): BloodPressureStandard? {
        val name = raw as? String ?: return null
        return runCatching { BloodPressureStandard.valueOf(name) }.getOrNull()
    }

    private fun buildAlertConfig(documents: List<com.google.firebase.firestore.DocumentSnapshot>): AlertConfig {
        val byType = documents.associateBy { it.id }
        val settings = MeasurementType.values().map { type ->
            val doc = byType[type.name]
            val enabled = doc?.getBoolean("enabled") ?: true
            val rawMin = parseDouble(doc?.get("min"))
            val rawMax = parseDouble(doc?.get("max"))
            val isPressure = type == MeasurementType.PRESSURE
            val systolicMin = if (isPressure) parseDouble(doc?.get("systolicMin")) ?: rawMin else null
            val systolicMax = if (isPressure) parseDouble(doc?.get("systolicMax")) ?: rawMax else null
            val diastolicMin = if (isPressure) parseDouble(doc?.get("diastolicMin")) else null
            val diastolicMax = if (isPressure) parseDouble(doc?.get("diastolicMax")) else null
            val min = if (isPressure) systolicMin else rawMin
            val max = if (isPressure) systolicMax else rawMax
            val spikePercent = ((doc?.get("spikePercent") as? Number)?.toInt()) ?: 20
            val windowCount = ((doc?.get("windowCount") as? Number)?.toInt()) ?: 3
            val channels = parseChannels(doc?.get("channels"))
            val caregiverUids = parseStringList(doc?.get("caregiverUids"))

            val categoryEnabled = if (isPressure) (doc?.getBoolean("categoryEnabled") ?: true) else false
            val categoryThreshold = if (isPressure) {
                parseBpSeverity(doc?.get("categoryThreshold")) ?: BloodPressureSeverity.HTN1
            } else {
                BloodPressureSeverity.HTN1
            }
            val categoryCooldownMinutes = if (isPressure) {
                parseInt(doc?.get("categoryCooldownMinutes")) ?: 60
            } else {
                60
            }
            val bpStandard = if (isPressure) {
                parseBpStandard(doc?.get("bpStandard")) ?: BloodPressureStandard.ESC_ESH_OFFICE
            } else {
                BloodPressureStandard.ESC_ESH_OFFICE
            }

            AlertSetting(
                type = type,
                enabled = enabled,
                min = min,
                max = max,
                spikePercent = spikePercent,
                windowCount = windowCount,
                channels = if (channels.isEmpty()) setOf(AlertChannel.APP) else channels,
                caregiverIds = caregiverUids,
                systolicMin = systolicMin,
                systolicMax = systolicMax,
                diastolicMin = diastolicMin,
                diastolicMax = diastolicMax,
                categoryEnabled = categoryEnabled,
                categoryThreshold = categoryThreshold,
                categoryCooldownMinutes = categoryCooldownMinutes,
                bpStandard = bpStandard
            )
        }
        return AlertConfig(settings = settings)
    }

    private fun requireUid(): String = uidProvider.requirePatientUid()
}

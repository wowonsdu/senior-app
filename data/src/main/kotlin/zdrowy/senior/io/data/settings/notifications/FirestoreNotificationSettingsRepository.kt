package zdrowy.senior.io.data.settings.notifications

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
import zdrowy.senior.io.domain.alert.AlertSetting
import zdrowy.senior.io.domain.measurement.MeasurementType
import zdrowy.senior.io.domain.settings.notifications.NotificationSettings
import zdrowy.senior.io.domain.settings.notifications.NotificationSettingsRepository

class FirestoreNotificationSettingsRepository(
    private val uidProvider: PatientUidProvider
) : NotificationSettingsRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val defaultPressureSystolicMin = 90.0
    private val defaultPressureSystolicMax = 129.0
    private val defaultPressureDiastolicMin = 60.0
    private val defaultPressureDiastolicMax = 84.0

    override fun getNotificationSettings(): Single<NotificationSettings> {
        val uid = requireUid()
        val doc = newDoc(uid)
        return doc.get()
            .toSingle()
            .flatMap { snapshot ->
                val alerts = snapshot.get("alerts")
                if (snapshot.exists() && alerts != null) {
                    Single.just(parseSnapshot(snapshot.get("alerts")))
                } else {
                    val defaults = defaultSettings()
                    doc.set(
                        mapOf(
                            "alerts" to buildAlertsMap(defaults),
                            "updatedAt" to FieldValue.serverTimestamp()
                        ),
                        SetOptions.merge()
                    ).toCompletable().andThen(Single.just(defaults))
                }
            }
    }

    override fun observeNotificationSettings(): Observable<NotificationSettings> {
        return getNotificationSettings()
            .toObservable()
            .concatWith(observeNewDoc())
            .distinctUntilChanged()
    }

    override fun updateNotificationSettings(settings: NotificationSettings): Completable {
        val uid = requireUid()
        val doc = newDoc(uid)
        return doc.set(
            mapOf(
                "alerts" to buildAlertsMap(settings),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).toCompletable()
    }

    override fun setAlertEnabled(type: MeasurementType, enabled: Boolean): Completable {
        return setAlertFields(type, mapOf("enabled" to enabled))
    }

    override fun updateCriticalThresholds(type: MeasurementType, min: Double?, max: Double?): Completable {
        return if (type == MeasurementType.PRESSURE) {
            setAlertFields(type, mapOf("systolicMin" to min, "systolicMax" to max))
        } else {
            setAlertFields(type, mapOf("min" to min, "max" to max))
        }
    }

    override fun updateSpikeRules(type: MeasurementType, percent: Int, windowCount: Int): Completable {
        return setAlertFields(type, mapOf("spikePercent" to percent, "windowCount" to windowCount))
    }

    override fun updateSugarDropRules(dropDelta: Double?, dropWindowMinutes: Int?): Completable {
        return setAlertFields(
            MeasurementType.SUGAR,
            mapOf(
                "dropDelta" to dropDelta,
                "dropWindowMinutes" to dropWindowMinutes
            )
        )
    }

    override fun updateAlertChannels(type: MeasurementType, channels: Set<AlertChannel>): Completable {
        return setAlertFields(type, mapOf("channels" to channels.map { it.name }))
    }

    override fun updateAlertCaregivers(type: MeasurementType, caregiverIds: List<String>): Completable {
        return setAlertFields(type, mapOf("caregiverUids" to caregiverIds))
    }

    override fun updateBloodPressureCriticalThresholds(
        systolicMin: Double?,
        systolicMax: Double?,
        diastolicMin: Double?,
        diastolicMax: Double?
    ): Completable {
        return setAlertFields(
            MeasurementType.PRESSURE,
            mapOf(
                "systolicMin" to systolicMin,
                "systolicMax" to systolicMax,
                "diastolicMin" to diastolicMin,
                "diastolicMax" to diastolicMax
            )
        )
    }

    private fun observeNewDoc(): Observable<NotificationSettings> {
        return Observable.create { emitter ->
            val uid = try {
                requireUid()
            } catch (error: Throwable) {
                emitter.onError(error)
                return@create
            }
            val doc = newDoc(uid)
            val registration = doc.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!emitter.isDisposed) emitter.onError(error)
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) {
                    val defaults = defaultSettings()
                    doc.set(
                        mapOf(
                            "alerts" to buildAlertsMap(defaults),
                            "updatedAt" to FieldValue.serverTimestamp()
                        ),
                        SetOptions.merge()
                    )
                    if (!emitter.isDisposed) emitter.onNext(defaults)
                    return@addSnapshotListener
                }
                val parsed = parseSnapshot(snapshot.get("alerts"))
                if (!emitter.isDisposed) emitter.onNext(parsed)
            }
            emitter.setCancellable { registration.remove() }
        }
    }

    private fun setAlertFields(type: MeasurementType, fields: Map<String, Any?>): Completable {
        val uid = requireUid()
        val doc = newDoc(uid)

        val timestamp = FieldValue.serverTimestamp()
        val updatePayload = fields.entries.associate { (key, value) ->
            "alerts.${type.name}.$key" to value
        }.toMutableMap()
        updatePayload["alerts.${type.name}.updatedAt"] = timestamp
        updatePayload["updatedAt"] = timestamp

        return doc.update(updatePayload).toCompletable()
            .onErrorResumeNext {
                val typePayload = buildAlertMapForSetting(defaultSetting(type)).toMutableMap()
                fields.forEach { (key, value) -> typePayload[key] = value }
                typePayload["updatedAt"] = timestamp
                doc.set(
                    mapOf(
                        "alerts" to mapOf(type.name to typePayload),
                        "updatedAt" to timestamp
                    ),
                    SetOptions.merge()
                ).toCompletable()
            }
    }

    private fun newDoc(uid: String) = firestore.collection(FirestorePaths.USERS)
        .document(uid)
        .collection(FirestorePaths.SETTINGS)
        .document(FirestorePaths.NOTIFICATIONS)

    private fun parseSnapshot(rawAlerts: Any?): NotificationSettings {
        val alerts = (rawAlerts as? Map<*, *>) ?: return defaultSettings()
        val settings = MeasurementType.values().map { type ->
            val raw = alerts[type.name] as? Map<*, *>
            buildNewSetting(type, raw)
        }
        return NotificationSettings(alerts = settings)
    }

    private fun buildNewSetting(type: MeasurementType, raw: Map<*, *>?): AlertSetting {
        if (raw == null) return defaultSetting(type)
        val enabled = raw["enabled"] as? Boolean ?: true
        val isPressure = type == MeasurementType.PRESSURE
        val rawMin = if (isPressure) null else parseDouble(raw["min"])
        val rawMax = if (isPressure) null else parseDouble(raw["max"])
        val systolicMin = if (isPressure) parseDouble(raw["systolicMin"]) else null
        val systolicMax = if (isPressure) parseDouble(raw["systolicMax"]) else null
        val diastolicMin = if (isPressure) parseDouble(raw["diastolicMin"]) else null
        val diastolicMax = if (isPressure) parseDouble(raw["diastolicMax"]) else null
        val min = rawMin
        val max = rawMax
        val spikePercent = (raw["spikePercent"] as? Number)?.toInt() ?: 20
        val windowCount = (raw["windowCount"] as? Number)?.toInt() ?: 3
        val dropDelta = if (type == MeasurementType.SUGAR) parseDouble(raw["dropDelta"]) else null
        val dropWindowMinutes = if (type == MeasurementType.SUGAR) parseInt(raw["dropWindowMinutes"]) else null
        val channels = if (raw.containsKey("channels")) parseChannels(raw["channels"]) else null
        val caregiverUids = parseStringList(raw["caregiverUids"])

        return AlertSetting(
            type = type,
            enabled = enabled,
            min = min,
            max = max,
            spikePercent = spikePercent,
            windowCount = windowCount,
            dropDelta = dropDelta,
            dropWindowMinutes = dropWindowMinutes,
            channels = channels ?: setOf(AlertChannel.APP),
            caregiverIds = caregiverUids,
            systolicMin = systolicMin,
            systolicMax = systolicMax,
            diastolicMin = diastolicMin,
            diastolicMax = diastolicMax
        )
    }

    private fun buildAlertsMap(settings: NotificationSettings): Map<String, Any?> {
        return MeasurementType.values().associate { type ->
            val setting = settings.alert(type) ?: defaultSetting(type)
            type.name to buildAlertMapForSetting(setting)
        }
    }

    private fun buildAlertMapForSetting(setting: AlertSetting): Map<String, Any?> {
        val isPressure = setting.type == MeasurementType.PRESSURE
        val payload = mutableMapOf<String, Any?>(
            "enabled" to setting.enabled,
            "spikePercent" to setting.spikePercent,
            "windowCount" to setting.windowCount,
            "channels" to setting.channels.map { it.name },
            "caregiverUids" to setting.caregiverIds,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (isPressure) {
            payload["systolicMin"] = setting.systolicMin
            payload["systolicMax"] = setting.systolicMax
            payload["diastolicMin"] = setting.diastolicMin
            payload["diastolicMax"] = setting.diastolicMax
        } else {
            payload["min"] = setting.min
            payload["max"] = setting.max
        }
        if (setting.type == MeasurementType.SUGAR) {
            payload["dropDelta"] = setting.dropDelta
            payload["dropWindowMinutes"] = setting.dropWindowMinutes
        }
        return payload
    }

    private fun defaultSettings(): NotificationSettings =
        NotificationSettings(alerts = MeasurementType.values().map { defaultSetting(it) })

    private fun defaultSetting(type: MeasurementType): AlertSetting =
        if (type == MeasurementType.PRESSURE) {
            AlertSetting(
                type = type,
                enabled = true,
                min = null,
                max = null,
                spikePercent = 20,
                windowCount = 3,
                channels = setOf(AlertChannel.APP),
                caregiverIds = emptyList(),
                systolicMin = defaultPressureSystolicMin,
                systolicMax = defaultPressureSystolicMax,
                diastolicMin = defaultPressureDiastolicMin,
                diastolicMax = defaultPressureDiastolicMax
            )
        } else {
            AlertSetting(
                type = type,
                enabled = true,
                min = null,
                max = null,
                spikePercent = 20,
                windowCount = 3,
                channels = setOf(AlertChannel.APP),
                caregiverIds = emptyList()
            )
        }

    private fun parseChannels(raw: Any?): Set<AlertChannel> {
        val list = (raw as? List<*>)?.mapNotNull { it as? String }.orEmpty()
        return list.mapNotNull { name -> runCatching { AlertChannel.valueOf(name) }.getOrNull() }.toSet()
    }

    private fun parseStringList(raw: Any?): List<String> =
        (raw as? List<*>)?.mapNotNull { it as? String }.orEmpty()

    private fun parseDouble(raw: Any?): Double? = (raw as? Number)?.toDouble()
    private fun parseInt(raw: Any?): Int? = (raw as? Number)?.toInt()

    private fun requireUid(): String = uidProvider.requirePatientUid()
}

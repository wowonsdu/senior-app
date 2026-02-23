package zdrowy.senior.io.data.alert

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import zdrowy.senior.io.domain.alert.AlertChannel
import zdrowy.senior.io.domain.alert.AlertConfig
import zdrowy.senior.io.domain.alert.AlertRepository
import zdrowy.senior.io.domain.alert.AlertSetting
import zdrowy.senior.io.domain.measurement.MeasurementType
import zdrowy.senior.io.domain.measurement.BloodPressureSeverity
import zdrowy.senior.io.domain.measurement.BloodPressureStandard

class InMemoryAlertRepository : AlertRepository {
    private var config: AlertConfig = AlertConfig(settings = defaultSettings())
    private val configSubject = BehaviorSubject.createDefault(config)

    override fun getAlertConfig(): Single<AlertConfig> = Single.just(config)

    override fun observeAlertConfig(): Observable<AlertConfig> = configSubject.hide()

    override fun updateAlertConfig(config: AlertConfig): Completable {
        this.config = config
        configSubject.onNext(config)
        return Completable.complete()
    }

    override fun setAlertEnabled(type: MeasurementType, enabled: Boolean): Completable {
        config = config.copy(settings = updateSetting(type) { it.copy(enabled = enabled) })
        configSubject.onNext(config)
        return Completable.complete()
    }

    override fun updateCriticalThresholds(type: MeasurementType, min: Double?, max: Double?): Completable {
        config = config.copy(settings = updateSetting(type) { it.copy(min = min, max = max) })
        configSubject.onNext(config)
        return Completable.complete()
    }

    override fun updateSpikeRules(type: MeasurementType, percent: Int, windowCount: Int): Completable {
        config = config.copy(settings = updateSetting(type) {
            it.copy(spikePercent = percent, windowCount = windowCount)
        })
        configSubject.onNext(config)
        return Completable.complete()
    }

    override fun updateAlertChannels(type: MeasurementType, channels: Set<AlertChannel>): Completable {
        config = config.copy(settings = updateSetting(type) { it.copy(channels = channels) })
        configSubject.onNext(config)
        return Completable.complete()
    }

    override fun updateAlertCaregivers(type: MeasurementType, caregiverIds: List<String>): Completable {
        config = config.copy(settings = updateSetting(type) { it.copy(caregiverIds = caregiverIds) })
        configSubject.onNext(config)
        return Completable.complete()
    }

    override fun updateBloodPressureCriticalThresholds(
        systolicMin: Double?,
        systolicMax: Double?,
        diastolicMin: Double?,
        diastolicMax: Double?
    ): Completable {
        config = config.copy(settings = updateSetting(MeasurementType.PRESSURE) {
            it.copy(
                min = systolicMin,
                max = systolicMax,
                systolicMin = systolicMin,
                systolicMax = systolicMax,
                diastolicMin = diastolicMin,
                diastolicMax = diastolicMax
            )
        })
        configSubject.onNext(config)
        return Completable.complete()
    }

    override fun updateBloodPressureCategoryRules(
        enabled: Boolean,
        threshold: BloodPressureSeverity,
        cooldownMinutes: Int,
        standard: BloodPressureStandard
    ): Completable {
        config = config.copy(settings = updateSetting(MeasurementType.PRESSURE) {
            it.copy(
                categoryEnabled = enabled,
                categoryThreshold = threshold,
                categoryCooldownMinutes = cooldownMinutes,
                bpStandard = standard
            )
        })
        configSubject.onNext(config)
        return Completable.complete()
    }

    private fun updateSetting(
        type: MeasurementType,
        transform: (AlertSetting) -> AlertSetting
    ): List<AlertSetting> {
        return config.settings.map { setting ->
            if (setting.type == type) transform(setting) else setting
        }
    }

    private fun defaultSettings(): List<AlertSetting> {
        return MeasurementType.values().map { type ->
            if (type == MeasurementType.PRESSURE) {
                AlertSetting(
                    type = type,
                    enabled = true,
                    min = 90.0,
                    max = 180.0,
                    spikePercent = 15,
                    windowCount = 3,
                    channels = setOf(AlertChannel.APP),
                    caregiverIds = emptyList(),
                    systolicMin = 90.0,
                    systolicMax = 180.0,
                    diastolicMin = 60.0,
                    diastolicMax = 110.0,
                    categoryEnabled = true,
                    categoryThreshold = BloodPressureSeverity.HTN1,
                    categoryCooldownMinutes = 60,
                    bpStandard = BloodPressureStandard.ESC_ESH_OFFICE
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
        }
    }
}

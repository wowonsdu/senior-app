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

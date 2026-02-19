package zdrowy.senior.io.data.alert

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.domain.alert.AlertChannel
import zdrowy.senior.io.domain.alert.AlertConfig
import zdrowy.senior.io.domain.alert.AlertRepository
import zdrowy.senior.io.domain.alert.AlertSetting
import zdrowy.senior.io.domain.measurement.MeasurementType

class InMemoryAlertRepository : AlertRepository {
    private var config: AlertConfig = AlertConfig(settings = defaultSettings())

    override fun getAlertConfig(): Single<AlertConfig> = Single.just(config)

    override fun updateAlertConfig(config: AlertConfig): Completable {
        this.config = config
        return Completable.complete()
    }

    override fun setAlertEnabled(type: MeasurementType, enabled: Boolean): Completable {
        config = config.copy(settings = updateSetting(type) { it.copy(enabled = enabled) })
        return Completable.complete()
    }

    override fun updateCriticalThresholds(type: MeasurementType, min: Double?, max: Double?): Completable {
        config = config.copy(settings = updateSetting(type) { it.copy(min = min, max = max) })
        return Completable.complete()
    }

    override fun updateSpikeRules(type: MeasurementType, percent: Int, windowCount: Int): Completable {
        config = config.copy(settings = updateSetting(type) {
            it.copy(spikePercent = percent, windowCount = windowCount)
        })
        return Completable.complete()
    }

    override fun updateAlertChannels(type: MeasurementType, channels: Set<AlertChannel>): Completable {
        config = config.copy(settings = updateSetting(type) { it.copy(channels = channels) })
        return Completable.complete()
    }

    override fun updateAlertCaregivers(type: MeasurementType, caregiverIds: List<String>): Completable {
        config = config.copy(settings = updateSetting(type) { it.copy(caregiverIds = caregiverIds) })
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

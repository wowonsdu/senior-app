package zdrowy.senior.io.domain.settings.notifications

import io.reactivex.rxjava3.core.Completable
import zdrowy.senior.io.domain.measurement.MeasurementType

class UpdateSpikeRulesUseCase(
    private val repository: NotificationSettingsRepository
) {
    operator fun invoke(type: MeasurementType, percent: Int, windowCount: Int): Completable =
        repository.updateSpikeRules(type, percent, windowCount)
}


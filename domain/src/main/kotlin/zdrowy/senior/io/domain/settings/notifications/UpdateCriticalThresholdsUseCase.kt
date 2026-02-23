package zdrowy.senior.io.domain.settings.notifications

import io.reactivex.rxjava3.core.Completable
import zdrowy.senior.io.domain.measurement.MeasurementType

class UpdateCriticalThresholdsUseCase(
    private val repository: NotificationSettingsRepository
) {
    operator fun invoke(type: MeasurementType, min: Double?, max: Double?): Completable =
        repository.updateCriticalThresholds(type, min, max)
}


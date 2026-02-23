package zdrowy.senior.io.domain.settings.notifications

import io.reactivex.rxjava3.core.Completable
import zdrowy.senior.io.domain.measurement.MeasurementType

class SetAlertEnabledUseCase(
    private val repository: NotificationSettingsRepository
) {
    operator fun invoke(type: MeasurementType, enabled: Boolean): Completable =
        repository.setAlertEnabled(type, enabled)
}


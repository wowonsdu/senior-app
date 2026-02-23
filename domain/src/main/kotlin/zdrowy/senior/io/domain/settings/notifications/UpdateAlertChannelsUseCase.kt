package zdrowy.senior.io.domain.settings.notifications

import io.reactivex.rxjava3.core.Completable
import zdrowy.senior.io.domain.alert.AlertChannel
import zdrowy.senior.io.domain.measurement.MeasurementType

class UpdateAlertChannelsUseCase(
    private val repository: NotificationSettingsRepository
) {
    operator fun invoke(type: MeasurementType, channels: Set<AlertChannel>): Completable =
        repository.updateAlertChannels(type, channels)
}


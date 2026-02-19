package zdrowy.senior.io.domain.alert

import io.reactivex.rxjava3.core.Completable
import zdrowy.senior.io.domain.measurement.MeasurementType

class UpdateAlertChannelsUseCase(
    private val repository: AlertRepository
) {
    operator fun invoke(type: MeasurementType, channels: Set<AlertChannel>): Completable =
        repository.updateAlertChannels(type, channels)
}

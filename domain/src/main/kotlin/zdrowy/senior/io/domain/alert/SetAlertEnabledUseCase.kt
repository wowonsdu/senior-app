package zdrowy.senior.io.domain.alert

import io.reactivex.rxjava3.core.Completable
import zdrowy.senior.io.domain.measurement.MeasurementType

class SetAlertEnabledUseCase(
    private val repository: AlertRepository
) {
    operator fun invoke(type: MeasurementType, enabled: Boolean): Completable =
        repository.setAlertEnabled(type, enabled)
}

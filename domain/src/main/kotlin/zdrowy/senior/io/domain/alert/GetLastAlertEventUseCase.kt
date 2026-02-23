package zdrowy.senior.io.domain.alert

import io.reactivex.rxjava3.core.Maybe
import zdrowy.senior.io.domain.measurement.MeasurementType

class GetLastAlertEventUseCase(
    private val repository: AlertEventRepository
) {
    operator fun invoke(type: MeasurementType, severity: AlertSeverity): Maybe<AlertEvent> =
        repository.getLastEvent(type, severity)
}


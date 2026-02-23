package zdrowy.senior.io.domain.alert

import io.reactivex.rxjava3.core.Observable
import zdrowy.senior.io.domain.measurement.MeasurementType

class ObserveRecentAlertEventsUseCase(
    private val repository: AlertEventRepository
) {
    operator fun invoke(
        type: MeasurementType,
        limit: Int,
        severity: AlertSeverity? = null
    ): Observable<List<AlertEvent>> = repository.observeRecentEvents(type, limit, severity)
}


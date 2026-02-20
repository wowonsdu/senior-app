package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Observable
import zdrowy.senior.io.domain.history.DateRange

class ObserveMeasurementHistoryUseCase(
    private val repository: MeasurementRepository
) {
    operator fun invoke(
        types: List<MeasurementType>?,
        dateRange: DateRange?
    ): Observable<List<Measurement>> = repository.observeMeasurementHistory(types, dateRange)
}

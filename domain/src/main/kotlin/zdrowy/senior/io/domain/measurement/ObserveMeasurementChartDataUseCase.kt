package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Observable
import zdrowy.senior.io.domain.history.ChartSeries
import zdrowy.senior.io.domain.history.DateRange

class ObserveMeasurementChartDataUseCase(
    private val repository: MeasurementRepository
) {
    operator fun invoke(
        types: List<MeasurementType>?,
        dateRange: DateRange?
    ): Observable<List<ChartSeries>> = repository.observeMeasurementChartData(types, dateRange)
}

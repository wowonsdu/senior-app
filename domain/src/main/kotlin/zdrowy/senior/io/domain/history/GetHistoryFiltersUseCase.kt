package zdrowy.senior.io.domain.history

import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.domain.measurement.MeasurementRepository

class GetHistoryFiltersUseCase(
    private val repository: MeasurementRepository
) {
    operator fun invoke(): Single<HistoryFilterState> = repository.getHistoryFilters()
}

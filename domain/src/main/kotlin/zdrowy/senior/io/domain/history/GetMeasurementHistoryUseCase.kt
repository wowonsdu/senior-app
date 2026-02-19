package zdrowy.senior.io.domain.history

import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.domain.measurement.Measurement
import zdrowy.senior.io.domain.measurement.MeasurementRepository
import zdrowy.senior.io.domain.measurement.MeasurementType

class GetMeasurementHistoryUseCase(
    private val repository: MeasurementRepository
) {
    operator fun invoke(
        types: List<MeasurementType>? = null,
        dateRange: DateRange? = null
    ): Single<List<Measurement>> = repository.getMeasurementHistory(types, dateRange)
}

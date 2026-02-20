package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Observable

class ObserveRecentMeasurementsUseCase(
    private val repository: MeasurementRepository
) {
    operator fun invoke(
        limit: Int,
        types: List<MeasurementType>? = null
    ): Observable<List<Measurement>> = repository.observeRecentMeasurements(limit, types)
}

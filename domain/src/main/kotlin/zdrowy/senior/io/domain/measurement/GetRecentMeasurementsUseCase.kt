package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Single

class GetRecentMeasurementsUseCase(
    private val repository: MeasurementRepository
) {
    operator fun invoke(
        limit: Int,
        types: List<MeasurementType>? = null
    ): Single<List<Measurement>> = repository.getRecentMeasurements(limit, types)
}

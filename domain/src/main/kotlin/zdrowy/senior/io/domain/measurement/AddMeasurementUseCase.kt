package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Single

class AddMeasurementUseCase(
    private val repository: MeasurementRepository
) {
    operator fun invoke(
        type: MeasurementType,
        value: Double,
        timestamp: Long,
        source: MeasurementSource
    ): Single<String> = repository.addMeasurement(type, value, timestamp, source)
}

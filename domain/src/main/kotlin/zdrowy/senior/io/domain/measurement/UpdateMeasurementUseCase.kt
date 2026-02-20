package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Single

class UpdateMeasurementUseCase(
    private val repository: MeasurementRepository
) {
    operator fun invoke(
        id: String,
        type: MeasurementType,
        value: Double,
        timestamp: Long
    ): Single<Unit> = repository.updateMeasurement(id, type, value, timestamp)
}

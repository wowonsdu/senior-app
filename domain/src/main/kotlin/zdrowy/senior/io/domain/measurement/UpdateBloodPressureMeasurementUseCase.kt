package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Single

class UpdateBloodPressureMeasurementUseCase(
    private val repository: MeasurementRepository
) {
    operator fun invoke(
        id: String,
        systolic: Int,
        diastolic: Int,
        timestamp: Long
    ): Single<Unit> = repository.updateBloodPressureMeasurement(
        id = id,
        systolic = systolic,
        diastolic = diastolic,
        timestamp = timestamp
    )
}

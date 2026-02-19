package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Single

class AddBloodPressureMeasurementUseCase(
    private val repository: MeasurementRepository
) {
    operator fun invoke(
        systolic: Int,
        diastolic: Int,
        timestamp: Long,
        source: MeasurementSource
    ): Single<String> = repository.addBloodPressureMeasurement(systolic, diastolic, timestamp, source)
}

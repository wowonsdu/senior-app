package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Single

class DeleteMeasurementUseCase(
    private val repository: MeasurementRepository
) {
    operator fun invoke(id: String): Single<Unit> = repository.deleteMeasurement(id)
}

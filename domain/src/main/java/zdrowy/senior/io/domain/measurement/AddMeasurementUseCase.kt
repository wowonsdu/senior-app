package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Completable

class AddMeasurementUseCase(
    private val repository: MeasurementRepository
) {
    fun execute(patientId: String, type: MeasurementType, valueRaw: String): Completable {
        return repository.addMeasurement(patientId, type, valueRaw)
    }
}

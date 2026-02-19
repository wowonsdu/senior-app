package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Observable

class ObserveMeasurementsUseCase(
    private val repository: MeasurementRepository
) {
    fun execute(patientId: String): Observable<List<Measurement>> {
        return repository.observeMeasurements(patientId)
    }
}

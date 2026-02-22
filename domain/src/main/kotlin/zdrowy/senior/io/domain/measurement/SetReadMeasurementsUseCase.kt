package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Completable

class SetReadMeasurementsUseCase(
    private val repository: MeasurementReadStateRepository
) {
    operator fun invoke(patientUid: String, measurementIds: List<String>): Completable {
        return repository.setReadMeasurements(patientUid, measurementIds)
    }
}

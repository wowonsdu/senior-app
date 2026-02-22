package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Completable

class MarkMeasurementReadUseCase(
    private val repository: MeasurementReadStateRepository
) {
    operator fun invoke(patientUid: String, measurementId: String): Completable {
        return repository.markMeasurementRead(patientUid, measurementId)
    }
}

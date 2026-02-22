package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Completable

class SetMeasurementReadStateUseCase(
    private val repository: MeasurementReadStateRepository
) {
    operator fun invoke(patientUid: String, timestampMs: Long): Completable {
        return repository.setLastReadAt(patientUid, timestampMs)
    }
}

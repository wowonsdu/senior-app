package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Observable

class ObserveMeasurementReadStateUseCase(
    private val repository: MeasurementReadStateRepository
) {
    operator fun invoke(patientUid: String): Observable<MeasurementReadState> {
        return repository.observeReadState(patientUid)
    }
}

package zdrowy.senior.io.domain.agent

import io.reactivex.rxjava3.core.Observable

class ObserveDoctorsByUidUseCase(
    private val repository: DoctorByUidRepository
) {
    operator fun invoke(patientUid: String): Observable<List<Agent>> =
        repository.observeDoctors(patientUid)
}

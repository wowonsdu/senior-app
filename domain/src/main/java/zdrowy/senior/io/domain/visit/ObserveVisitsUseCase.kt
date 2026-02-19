package zdrowy.senior.io.domain.visit

import io.reactivex.rxjava3.core.Observable

class ObserveVisitsUseCase(
    private val repository: VisitRepository
) {
    fun execute(patientId: String): Observable<List<Visit>> {
        return repository.observeVisits(patientId)
    }
}

package zdrowy.senior.io.domain.visit

import io.reactivex.rxjava3.core.Observable

class ObserveVisitsUseCase(
    private val repository: VisitRepository
) {
    operator fun invoke(): Observable<List<Visit>> = repository.observeVisits()
}

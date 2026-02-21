package zdrowy.senior.io.domain.carelink

import io.reactivex.rxjava3.core.Observable

class ObserveCareLinksUseCase(
    private val repository: CareLinkRepository
) {
    operator fun invoke(): Observable<List<CareLink>> = repository.observeCareLinks()
}

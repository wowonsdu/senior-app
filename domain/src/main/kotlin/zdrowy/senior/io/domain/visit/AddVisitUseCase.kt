package zdrowy.senior.io.domain.visit

import io.reactivex.rxjava3.core.Single

class AddVisitUseCase(
    private val repository: VisitRepository
) {
    operator fun invoke(draft: VisitDraft): Single<String> = repository.addVisit(draft)
}

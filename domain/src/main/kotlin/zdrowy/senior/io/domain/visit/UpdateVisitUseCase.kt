package zdrowy.senior.io.domain.visit

import io.reactivex.rxjava3.core.Completable

class UpdateVisitUseCase(
    private val repository: VisitRepository
) {
    operator fun invoke(visitId: String, update: VisitUpdate): Completable =
        repository.updateVisit(visitId, update)
}

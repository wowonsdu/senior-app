package zdrowy.senior.io.domain.visit

import io.reactivex.rxjava3.core.Completable

class RemoveVisitUseCase(
    private val repository: VisitRepository
) {
    operator fun invoke(visitId: String): Completable = repository.removeVisit(visitId)
}

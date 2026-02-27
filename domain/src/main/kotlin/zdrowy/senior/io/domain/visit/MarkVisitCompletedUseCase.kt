package zdrowy.senior.io.domain.visit

import io.reactivex.rxjava3.core.Completable

class MarkVisitCompletedUseCase(
    private val repository: VisitRepository
) {
    operator fun invoke(visitId: String, completedAtMs: Long): Completable =
        repository.markVisitCompleted(visitId, completedAtMs)
}

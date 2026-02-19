package zdrowy.senior.io.domain.visit

import io.reactivex.rxjava3.core.Completable

class AddVisitUseCase(
    private val repository: VisitRepository
) {
    fun execute(patientId: String, title: String, dateTime: String, notes: String?): Completable {
        return repository.addVisit(patientId, title, dateTime, notes)
    }
}

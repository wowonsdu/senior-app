package zdrowy.senior.io.domain.contact

import io.reactivex.rxjava3.core.Completable

class DeleteContactUseCase(
    private val repository: ContactRepository
) {
    fun execute(patientId: String, contactId: String): Completable {
        return repository.deleteContact(patientId, contactId)
    }
}

package zdrowy.senior.io.domain.contact

import io.reactivex.rxjava3.core.Completable

class AddContactUseCase(
    private val repository: ContactRepository
) {
    fun execute(
        patientId: String,
        type: ContactType,
        name: String,
        phone: String?,
        specialization: String?
    ): Completable {
        return repository.addContact(patientId, type, name, phone, specialization)
    }
}

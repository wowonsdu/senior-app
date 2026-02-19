package zdrowy.senior.io.domain.contact

import io.reactivex.rxjava3.core.Observable

class ObserveContactsUseCase(
    private val repository: ContactRepository
) {
    fun execute(patientId: String): Observable<List<Contact>> {
        return repository.observeContacts(patientId)
    }
}

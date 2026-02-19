package zdrowy.senior.io.domain.contact

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface ContactRepository {
    fun observeContacts(patientId: String): Observable<List<Contact>>
    fun addContact(
        patientId: String,
        type: ContactType,
        name: String,
        phone: String?,
        specialization: String?
    ): Completable
    fun deleteContact(patientId: String, contactId: String): Completable
}

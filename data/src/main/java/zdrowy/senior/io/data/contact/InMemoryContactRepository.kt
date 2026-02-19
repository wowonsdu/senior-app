package zdrowy.senior.io.data.contact

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import zdrowy.senior.io.domain.contact.Contact
import zdrowy.senior.io.domain.contact.ContactRepository
import zdrowy.senior.io.domain.contact.ContactType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryContactRepository : ContactRepository {
    private val subjects = ConcurrentHashMap<String, BehaviorSubject<List<Contact>>>()

    override fun observeContacts(patientId: String): Observable<List<Contact>> {
        val subject = subjects.getOrPut(patientId) { BehaviorSubject.createDefault(emptyList()) }
        return subject.hide()
    }

    override fun addContact(
        patientId: String,
        type: ContactType,
        name: String,
        phone: String?,
        specialization: String?
    ): Completable {
        return Completable.fromAction {
            val subject = subjects.getOrPut(patientId) { BehaviorSubject.createDefault(emptyList()) }
            val current = subject.value ?: emptyList()
            val item = Contact(
                id = UUID.randomUUID().toString(),
                patientId = patientId,
                type = type,
                name = name,
                phone = phone,
                specialization = specialization
            )
            subject.onNext(listOf(item) + current)
        }
    }

    override fun deleteContact(patientId: String, contactId: String): Completable {
        return Completable.fromAction {
            val subject = subjects.getOrPut(patientId) { BehaviorSubject.createDefault(emptyList()) }
            val updated = (subject.value ?: emptyList()).filterNot { it.id == contactId }
            subject.onNext(updated)
        }
    }
}


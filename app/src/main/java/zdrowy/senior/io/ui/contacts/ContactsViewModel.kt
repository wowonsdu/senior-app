package zdrowy.senior.io.ui.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import zdrowy.senior.io.domain.contact.AddContactUseCase
import zdrowy.senior.io.domain.contact.ContactType
import zdrowy.senior.io.domain.contact.ObserveContactsUseCase
import zdrowy.senior.io.domain.session.GetActivePatientUseCase
import zdrowy.senior.io.domain.session.GetSessionUseCase
import zdrowy.senior.io.domain.session.UserRole

class ContactsViewModel : ViewModel(), KoinComponent {
    private val getSessionUseCase: GetSessionUseCase by inject()
    private val getActivePatientUseCase: GetActivePatientUseCase by inject()
    private val observeContactsUseCase: ObserveContactsUseCase by inject()
    private val addContactUseCase: AddContactUseCase by inject()

    private val disposables = CompositeDisposable()
    private val _contactsText = MutableLiveData<String>()
    val contactsText: LiveData<String> = _contactsText

    private var patientId: String? = null

    fun start() {
        val session = getSessionUseCase.execute()
        if (session == null) {
            _contactsText.value = "Brak sesji"
            return
        }
        patientId = when (session.role) {
            UserRole.PATIENT -> session.uid
            UserRole.CAREGIVER -> getActivePatientUseCase.execute()
        }
        val pid = patientId
        if (pid.isNullOrBlank()) {
            _contactsText.value = "Brak aktywnego pacjenta"
            return
        }
        val disposable = observeContactsUseCase.execute(pid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { list ->
                    _contactsText.value = if (list.isEmpty()) {
                        "Brak kontaktow"
                    } else {
                        list.joinToString(separator = "\n") { item ->
                            val spec = item.specialization?.let { " ($it)" } ?: ""
                            val phone = item.phone?.let { " - $it" } ?: ""
                            "${item.type} - ${item.name}$spec$phone"
                        }
                    }
                },
                { error ->
                    _contactsText.value = error.message ?: "Blad"
                }
            )
        disposables.add(disposable)
    }

    fun addContact(type: ContactType, name: String, phone: String?, specialization: String?) {
        val pid = patientId ?: return
        val disposable = addContactUseCase.execute(pid, type, name, phone, specialization)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, {})
        disposables.add(disposable)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}


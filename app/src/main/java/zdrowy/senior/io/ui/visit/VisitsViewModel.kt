package zdrowy.senior.io.ui.visit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import zdrowy.senior.io.domain.session.GetActivePatientUseCase
import zdrowy.senior.io.domain.session.GetSessionUseCase
import zdrowy.senior.io.domain.session.UserRole
import zdrowy.senior.io.domain.visit.AddVisitUseCase
import zdrowy.senior.io.domain.visit.ObserveVisitsUseCase

class VisitsViewModel : ViewModel(), KoinComponent {
    private val getSessionUseCase: GetSessionUseCase by inject()
    private val getActivePatientUseCase: GetActivePatientUseCase by inject()
    private val observeVisitsUseCase: ObserveVisitsUseCase by inject()
    private val addVisitUseCase: AddVisitUseCase by inject()

    private val disposables = CompositeDisposable()
    private val _visitsText = MutableLiveData<String>()
    val visitsText: LiveData<String> = _visitsText

    private var patientId: String? = null

    fun start() {
        val session = getSessionUseCase.execute()
        if (session == null) {
            _visitsText.value = "Brak sesji"
            return
        }
        patientId = when (session.role) {
            UserRole.PATIENT -> session.uid
            UserRole.CAREGIVER -> getActivePatientUseCase.execute()
        }
        val pid = patientId
        if (pid.isNullOrBlank()) {
            _visitsText.value = "Brak aktywnego pacjenta"
            return
        }
        val disposable = observeVisitsUseCase.execute(pid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { list ->
                    _visitsText.value = if (list.isEmpty()) {
                        "Brak wizyt"
                    } else {
                        list.joinToString(separator = "\n") { item ->
                            "${item.title} - ${item.dateTime}"
                        }
                    }
                },
                { error ->
                    _visitsText.value = error.message ?: "Blad"
                }
            )
        disposables.add(disposable)
    }

    fun addVisit(title: String, dateTime: String, notes: String?) {
        val pid = patientId ?: return
        val disposable = addVisitUseCase.execute(pid, title, dateTime, notes)
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


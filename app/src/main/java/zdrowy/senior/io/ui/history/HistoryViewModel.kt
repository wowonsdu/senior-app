package zdrowy.senior.io.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import zdrowy.senior.io.domain.measurement.ObserveMeasurementsUseCase
import zdrowy.senior.io.domain.session.GetActivePatientUseCase
import zdrowy.senior.io.domain.session.GetSessionUseCase
import zdrowy.senior.io.domain.session.UserRole

class HistoryViewModel : ViewModel(), KoinComponent {
    private val getSessionUseCase: GetSessionUseCase by inject()
    private val getActivePatientUseCase: GetActivePatientUseCase by inject()
    private val observeMeasurementsUseCase: ObserveMeasurementsUseCase by inject()

    private val disposables = CompositeDisposable()
    private val _historyText = MutableLiveData<String>()
    val historyText: LiveData<String> = _historyText

    fun start() {
        val session = getSessionUseCase.execute() ?: run {
            _historyText.value = "Brak sesji"
            return
        }
        val patientId = when (session.role) {
            UserRole.PATIENT -> session.uid
            UserRole.CAREGIVER -> getActivePatientUseCase.execute()
        }
        if (patientId.isNullOrBlank()) {
            _historyText.value = "Brak aktywnego pacjenta"
            return
        }
        val disposable = observeMeasurementsUseCase.execute(patientId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { list ->
                    _historyText.value = if (list.isEmpty()) {
                        "Brak pomiarow"
                    } else {
                        list.joinToString(separator = "\n") { item ->
                            "${item.type} - ${item.valueRaw} (${item.createdAt})"
                        }
                    }
                },
                { error ->
                    _historyText.value = error.message ?: "Blad"
                }
            )
        disposables.add(disposable)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}


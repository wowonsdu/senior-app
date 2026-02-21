package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.settings.ObservePersonalDataUseCase
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.domain.settings.UpsertPersonalDataUseCase

class PatientPersonalDataViewModel(
    private val observePersonalData: ObservePersonalDataUseCase,
    private val upsertPersonalData: UpsertPersonalDataUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _personalData = MutableLiveData<PersonalData?>()
    val personalData: LiveData<PersonalData?> = _personalData
    private var started = false

    fun start() {
        if (started) return
        started = true
        disposables.add(
            observePersonalData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data ->
                    _personalData.value = data
                }, { _personalData.value = null })
        )
    }

    fun upsertPersonalData(data: PersonalData, onDone: () -> Unit) {
        disposables.add(
            upsertPersonalData(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onDone() }, { onDone() })
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}


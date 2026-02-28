package zdrowy.senior.io.ui.caregiver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.carelink.CareLinkCodeType
import zdrowy.senior.io.domain.carelink.CareLinkDraft
import zdrowy.senior.io.domain.carelink.ConsumeCareLinkCodeUseCase
import zdrowy.senior.io.domain.carelink.CreateDependentProfileUseCase
import zdrowy.senior.io.domain.user.SetActivePatientUseCase

class CaregiverAddDependentViewModel(
    private val consumeCareLinkCode: ConsumeCareLinkCodeUseCase,
    private val createDependentProfile: CreateDependentProfileUseCase,
    private val setActivePatient: SetActivePatientUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _formErrorMessage = MutableLiveData<String?>()
    val formErrorMessage: LiveData<String?> = _formErrorMessage
    private val _codeErrorMessage = MutableLiveData<String?>()
    val codeErrorMessage: LiveData<String?> = _codeErrorMessage
    private val _closeScreen = MutableLiveData<Boolean>()
    val closeScreen: LiveData<Boolean> = _closeScreen

    fun createDependent(draft: CareLinkDraft) {
        disposables.add(
            createDependentProfile(draft)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _formErrorMessage.value = null
                    _closeScreen.value = true
                }, { error ->
                    _formErrorMessage.value = error.message ?: "Blad zapisu podopiecznego"
                })
        )
    }

    fun linkByCode(code: String) {
        disposables.add(
            consumeCareLinkCode(code)
                .flatMapCompletable { link -> setActivePatient(link.patientUid) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _codeErrorMessage.value = null
                    _closeScreen.value = true
                }, { error ->
                    _codeErrorMessage.value = error.message ?: "Blad laczenia"
                })
        )
    }

    fun onFormErrorHandled() {
        _formErrorMessage.value = null
    }

    fun onCodeErrorHandled() {
        _codeErrorMessage.value = null
    }

    fun onCloseHandled() {
        _closeScreen.value = false
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}

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
import zdrowy.senior.io.domain.carelink.GenerateCareLinkCodeUseCase
import zdrowy.senior.io.domain.carelink.ObserveCareLinksUseCase
import zdrowy.senior.io.domain.user.SetActivePatientUseCase

class CaregiverAddDependentViewModel(
    private val generateCareLinkCode: GenerateCareLinkCodeUseCase,
    private val consumeCareLinkCode: ConsumeCareLinkCodeUseCase,
    private val setActivePatient: SetActivePatientUseCase,
    private val observeCareLinks: ObserveCareLinksUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _generatedCode = MutableLiveData<String>()
    val generatedCode: LiveData<String> = _generatedCode
    private val _formErrorMessage = MutableLiveData<String?>()
    val formErrorMessage: LiveData<String?> = _formErrorMessage
    private val _codeErrorMessage = MutableLiveData<String?>()
    val codeErrorMessage: LiveData<String?> = _codeErrorMessage
    private val _closeScreen = MutableLiveData<Boolean>()
    val closeScreen: LiveData<Boolean> = _closeScreen
    private var started = false
    private var baselineCount: Int? = null

    fun start() {
        if (started) return
        started = true
        disposables.add(
            observeCareLinks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ links ->
                    if (baselineCount == null) {
                        baselineCount = links.size
                        return@subscribe
                    }
                    if (links.size > (baselineCount ?: 0)) {
                        baselineCount = links.size
                        _closeScreen.value = true
                    }
                }, { error ->
                    _formErrorMessage.value = error.message
                })
        )
    }

    fun generateCode(draft: CareLinkDraft) {
        val ttlSeconds = 30L * 24 * 60 * 60
        disposables.add(
            generateCareLinkCode(CareLinkCodeType.CAREGIVER_TO_PATIENT, ttlSeconds, draft)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ code ->
                    _generatedCode.value = code.code
                    _formErrorMessage.value = null
                }, { error ->
                    _formErrorMessage.value = error.message ?: "Blad generowania kodu"
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

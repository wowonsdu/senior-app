package zdrowy.senior.io.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.carelink.CareLinkCodeType
import zdrowy.senior.io.domain.carelink.GetCareLinkCodeInfoUseCase

class PatientLoginViewModel(
    private val getCareLinkCodeInfo: GetCareLinkCodeInfoUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _resolvedPhone = MutableLiveData<String?>()
    val resolvedPhone: LiveData<String?> = _resolvedPhone
    private val _codeError = MutableLiveData<String?>()
    val codeError: LiveData<String?> = _codeError

    fun resolveCode(code: String) {
        disposables.add(
            getCareLinkCodeInfo(code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ info ->
                    if (info.type != CareLinkCodeType.CAREGIVER_TO_PATIENT) {
                        _codeError.value = "Nieprawidlowy kod"
                        return@subscribe
                    }
                    if (info.draftPhoneNumber.isBlank()) {
                        _codeError.value = "Nieprawidlowy kod"
                        return@subscribe
                    }
                    _resolvedPhone.value = info.draftPhoneNumber
                }, {
                    _codeError.value = "Nieprawidlowy kod"
                })
        )
    }

    fun onResolvedHandled() {
        _resolvedPhone.value = null
    }

    fun onErrorHandled() {
        _codeError.value = null
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}

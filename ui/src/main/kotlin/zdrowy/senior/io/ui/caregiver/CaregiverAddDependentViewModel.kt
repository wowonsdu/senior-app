package zdrowy.senior.io.ui.caregiver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.carelink.CareLinkCodeType
import zdrowy.senior.io.domain.carelink.CareLinkDraft
import zdrowy.senior.io.domain.carelink.GenerateCareLinkCodeUseCase

class CaregiverAddDependentViewModel(
    private val generateCareLinkCode: GenerateCareLinkCodeUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _generatedCode = MutableLiveData<String>()
    val generatedCode: LiveData<String> = _generatedCode
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun generateCode(draft: CareLinkDraft) {
        val ttlSeconds = 30L * 24 * 60 * 60
        disposables.add(
            generateCareLinkCode(CareLinkCodeType.CAREGIVER_TO_PATIENT, ttlSeconds, draft)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ code ->
                    _generatedCode.value = code.code
                    _errorMessage.value = null
                }, { error ->
                    _errorMessage.value = error.message ?: "Blad generowania kodu"
                })
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}

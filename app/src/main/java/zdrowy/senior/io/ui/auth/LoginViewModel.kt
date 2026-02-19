package zdrowy.senior.io.ui.auth

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import zdrowy.senior.io.domain.auth.SendOtpUseCase
import zdrowy.senior.io.domain.invite.ResolveInviteUseCase
import zdrowy.senior.io.ui.common.Event

class LoginViewModel : ViewModel(), KoinComponent {
    private val sendOtpUseCase: SendOtpUseCase by inject()
    private val resolveInviteUseCase: ResolveInviteUseCase by inject()
    private val disposables = CompositeDisposable()

    private val _uiState = MutableLiveData(LoginUiState())
    val uiState: LiveData<LoginUiState> = _uiState

    private val _verificationEvent = MutableLiveData<Event<LoginResult>>()
    val verificationEvent: LiveData<Event<LoginResult>> = _verificationEvent

    fun sendOtp(phoneE164: String, inviteCode: String?, activity: Activity) {
        _uiState.value = LoginUiState(loading = true, errorMessage = null)
        val disposable = if (inviteCode.isNullOrBlank()) {
            sendOtpUseCase.execute(phoneE164, activity)
                .map { verification -> LoginResult(verification.verificationId, null) }
        } else {
            resolveInviteUseCase.execute(inviteCode, phoneE164)
                .flatMap { resolution ->
                    sendOtpUseCase.execute(phoneE164, activity)
                        .map { verification -> LoginResult(verification.verificationId, resolution.inviteId) }
                }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    _uiState.value = LoginUiState(loading = false, errorMessage = null)
                    _verificationEvent.value = Event(result)
                },
                { error ->
                    _uiState.value = LoginUiState(loading = false, errorMessage = error.message)
                }
            )
        disposables.add(disposable)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}

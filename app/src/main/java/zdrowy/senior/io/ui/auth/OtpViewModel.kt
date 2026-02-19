package zdrowy.senior.io.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import zdrowy.senior.io.domain.auth.AuthSession
import zdrowy.senior.io.domain.auth.VerifyOtpUseCase
import zdrowy.senior.io.domain.invite.ClaimInviteUseCase
import zdrowy.senior.io.ui.common.Event

class OtpViewModel : ViewModel(), KoinComponent {
    private val verifyOtpUseCase: VerifyOtpUseCase by inject()
    private val claimInviteUseCase: ClaimInviteUseCase by inject()
    private val disposables = CompositeDisposable()

    private val _uiState = MutableLiveData(OtpUiState())
    val uiState: LiveData<OtpUiState> = _uiState

    private val _sessionEvent = MutableLiveData<Event<AuthSession>>()
    val sessionEvent: LiveData<Event<AuthSession>> = _sessionEvent

    fun verify(verificationId: String, code: String, inviteId: String?) {
        _uiState.value = OtpUiState(loading = true, errorMessage = null)
        val disposable = verifyOtpUseCase.execute(verificationId, code)
            .flatMap { session ->
                if (inviteId.isNullOrBlank()) {
                    io.reactivex.rxjava3.core.Single.just(session)
                } else {
                    claimInviteUseCase.execute(inviteId)
                        .andThen(io.reactivex.rxjava3.core.Single.just(session))
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { session ->
                    _uiState.value = OtpUiState(loading = false, errorMessage = null)
                    _sessionEvent.value = Event(session)
                },
                { error ->
                    _uiState.value = OtpUiState(loading = false, errorMessage = error.message)
                }
            )
        disposables.add(disposable)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}

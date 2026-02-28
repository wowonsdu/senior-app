package zdrowy.senior.io.ui.caregiver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.carelink.CareLink
import zdrowy.senior.io.domain.carelink.ConsumeCareLinkCodeUseCase
import zdrowy.senior.io.domain.carelink.ObserveCareLinksUseCase
import zdrowy.senior.io.domain.user.GetCurrentUserRoleUseCase
import zdrowy.senior.io.domain.user.SetActivePatientUseCase
import zdrowy.senior.io.domain.user.UserRole

class CaregiverLinkViewModel(
    private val observeCareLinks: ObserveCareLinksUseCase,
    private val consumeCareLinkCode: ConsumeCareLinkCodeUseCase,
    private val getCurrentUserRole: GetCurrentUserRoleUseCase,
    private val setActivePatient: SetActivePatientUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    private val _navTarget = MutableLiveData<CareLinkNavTarget?>()
    val navTarget: LiveData<CareLinkNavTarget?> = _navTarget
    private var started = false
    private var navigated = false
    private var fromAuthFlow = false

    fun start(fromAuth: Boolean) {
        if (started) return
        fromAuthFlow = fromAuth
        started = true
        disposables.add(
            observeCareLinks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ links ->
                    if (links.isNotEmpty() && !navigated) {
                        handleExistingLink(links.first())
                    }
                }, { error ->
                    _errorMessage.value = error.message
                })
        )
    }

    fun consumeCode(code: String) {
        if (code.isBlank()) {
            _errorMessage.value = "Pole wymagane"
            return
        }
        val digits = code.filter { it.isDigit() }
        if (digits.length != 6) {
            _errorMessage.value = "Kod musi miec 6 cyfr"
            return
        }
        disposables.add(
            consumeCareLinkCode(digits)
                .subscribeOn(Schedulers.io())
                .flatMap { link ->
                    getCurrentUserRole().flatMap { role ->
                        val setActive = if (role == UserRole.CAREGIVER || role == UserRole.PATIENT) {
                            setActivePatient(link.patientUid)
                        } else {
                            io.reactivex.rxjava3.core.Completable.complete()
                        }
                        setActive.andThen(io.reactivex.rxjava3.core.Single.just(role))
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _errorMessage.value = null
                    navigated = true
                    _navTarget.value = navTargetForRole(it)
                }, { error ->
                    _errorMessage.value = error.message ?: "Blad laczenia"
                })
        )
    }

    fun onNavigationHandled() {
        _navTarget.value = null
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    private fun handleExistingLink(link: CareLink) {
        disposables.add(
            getCurrentUserRole()
                .subscribeOn(Schedulers.io())
                .flatMap { role ->
                    val setActive = if (role == UserRole.CAREGIVER || role == UserRole.PATIENT) {
                        setActivePatient(link.patientUid)
                    } else {
                        io.reactivex.rxjava3.core.Completable.complete()
                    }
                    setActive.andThen(io.reactivex.rxjava3.core.Single.just(role))
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it == UserRole.CAREGIVER || fromAuthFlow) {
                        navigated = true
                        _navTarget.value = CareLinkNavTarget.PATIENT_HOME
                    }
                }, { error ->
                    _errorMessage.value = error.message
                })
        )
    }

    private fun navTargetForRole(role: UserRole): CareLinkNavTarget {
        return if (role == UserRole.CAREGIVER || fromAuthFlow) {
            CareLinkNavTarget.PATIENT_HOME
        } else {
            CareLinkNavTarget.POP_BACK
        }
    }
}

enum class CareLinkNavTarget {
    PATIENT_HOME,
    POP_BACK;
}

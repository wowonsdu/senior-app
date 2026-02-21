package zdrowy.senior.io.ui.caregiver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.carelink.CareLink
import zdrowy.senior.io.domain.carelink.CareLinkCodeType
import zdrowy.senior.io.domain.carelink.ConsumeCareLinkCodeUseCase
import zdrowy.senior.io.domain.carelink.GenerateCareLinkCodeUseCase
import zdrowy.senior.io.domain.carelink.ObserveCareLinksUseCase
import zdrowy.senior.io.domain.user.GetCurrentUserRoleUseCase
import zdrowy.senior.io.domain.user.SetActivePatientUseCase
import zdrowy.senior.io.domain.user.UserRole

class CaregiverLinkViewModel(
    private val observeCareLinks: ObserveCareLinksUseCase,
    private val generateCareLinkCode: GenerateCareLinkCodeUseCase,
    private val consumeCareLinkCode: ConsumeCareLinkCodeUseCase,
    private val getCurrentUserRole: GetCurrentUserRoleUseCase,
    private val setActivePatient: SetActivePatientUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _generatedCode = MutableLiveData<String>()
    val generatedCode: LiveData<String> = _generatedCode
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    private val _navTarget = MutableLiveData<CareLinkNavTarget?>()
    val navTarget: LiveData<CareLinkNavTarget?> = _navTarget
    private var started = false
    private var navigated = false

    fun start() {
        if (started) return
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

    fun generateCode() {
        disposables.add(
            getCurrentUserRole()
                .subscribeOn(Schedulers.io())
                .flatMap { role ->
                    val type = if (role == UserRole.CAREGIVER) {
                        CareLinkCodeType.CAREGIVER_TO_PATIENT
                    } else {
                        CareLinkCodeType.PATIENT_TO_CAREGIVER
                    }
                    generateCareLinkCode(type, 3600, null)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ code ->
                    _generatedCode.value = code.code
                    _errorMessage.value = null
                }, { error ->
                    _errorMessage.value = error.message ?: "Blad generowania kodu"
                })
        )
    }

    fun consumeCode(code: String) {
        if (code.isBlank()) {
            _errorMessage.value = "Pole wymagane"
            return
        }
        disposables.add(
            consumeCareLinkCode(code)
                .subscribeOn(Schedulers.io())
                .flatMap { link ->
                    getCurrentUserRole().flatMap { role ->
                        val setActive = if (role == UserRole.CAREGIVER) {
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
                    val setActive = if (role == UserRole.CAREGIVER) {
                        setActivePatient(link.patientUid)
                    } else {
                        io.reactivex.rxjava3.core.Completable.complete()
                    }
                    setActive.andThen(io.reactivex.rxjava3.core.Single.just(role))
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it == UserRole.CAREGIVER) {
                        navigated = true
                        _navTarget.value = CareLinkNavTarget.PATIENT_HOME
                    }
                }, { error ->
                    _errorMessage.value = error.message
                })
        )
    }

    private fun navTargetForRole(role: UserRole): CareLinkNavTarget {
        return if (role == UserRole.CAREGIVER) CareLinkNavTarget.PATIENT_HOME else CareLinkNavTarget.POP_BACK
    }
}

enum class CareLinkNavTarget {
    PATIENT_HOME,
    POP_BACK;
}

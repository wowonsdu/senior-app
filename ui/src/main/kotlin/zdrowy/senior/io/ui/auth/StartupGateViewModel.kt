package zdrowy.senior.io.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.user.EnsureCurrentUserRoleLoadedUseCase
import zdrowy.senior.io.domain.user.EnsureManagedPatientContextUseCase
import zdrowy.senior.io.domain.user.UserRole

sealed class StartupGateNavTarget {
    object PatientHome : StartupGateNavTarget()
    object CaregiverHome : StartupGateNavTarget()
    object RoleSelect : StartupGateNavTarget()
}

class StartupGateViewModel(
    private val ensureCurrentUserRoleLoaded: EnsureCurrentUserRoleLoadedUseCase,
    private val ensureManagedPatientContext: EnsureManagedPatientContextUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _navTarget = MutableLiveData<StartupGateNavTarget?>()
    val navTarget: LiveData<StartupGateNavTarget?> = _navTarget
    private var started = false

    fun start() {
        if (started) return
        started = true
        disposables.add(
            ensureCurrentUserRoleLoaded()
                .flatMap { role ->
                    ensureManagedPatientContext()
                        .onErrorComplete()
                        .andThen(io.reactivex.rxjava3.core.Single.just(role))
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ role ->
                    _navTarget.value = if (role == UserRole.CAREGIVER) {
                        StartupGateNavTarget.CaregiverHome
                    } else {
                        StartupGateNavTarget.PatientHome
                    }
                }, {
                    _navTarget.value = StartupGateNavTarget.RoleSelect
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
}

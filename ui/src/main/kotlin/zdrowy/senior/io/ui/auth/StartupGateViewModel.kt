package zdrowy.senior.io.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.user.EnsureCurrentUserRoleLoadedUseCase

sealed class StartupGateNavTarget {
    object Home : StartupGateNavTarget()
    object RoleSelect : StartupGateNavTarget()
}

class StartupGateViewModel(
    private val ensureCurrentUserRoleLoaded: EnsureCurrentUserRoleLoadedUseCase
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _navTarget.value = StartupGateNavTarget.Home
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

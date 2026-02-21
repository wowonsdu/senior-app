package zdrowy.senior.io.data.user

import android.content.Context
import android.content.SharedPreferences
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import zdrowy.senior.io.domain.user.CurrentUserRoleContext
import zdrowy.senior.io.domain.user.UserRole
import zdrowy.senior.io.domain.user.UserRoleState

class SharedPrefsCurrentUserRoleContext(
    appContext: Context
) : CurrentUserRoleContext {
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val subject = BehaviorSubject.createDefault(readState())

    override fun getRole(): UserRole? {
        val state = subject.value
        return if (state is UserRoleState.Available) state.role else null
    }

    override fun observeRole(): Observable<UserRoleState> = subject.hide()

    override fun setRole(role: UserRole): Completable {
        return Completable.fromAction {
            prefs.edit().putString(KEY_ROLE, role.name).apply()
            subject.onNext(UserRoleState.Available(role))
        }
    }

    override fun clearRole(): Completable {
        return Completable.fromAction {
            prefs.edit().remove(KEY_ROLE).apply()
            subject.onNext(UserRoleState.Missing)
        }
    }

    private fun readState(): UserRoleState {
        val raw = prefs.getString(KEY_ROLE, null) ?: return UserRoleState.Missing
        val role = runCatching { UserRole.valueOf(raw) }.getOrNull()
        return if (role == null) UserRoleState.Missing else UserRoleState.Available(role)
    }

    private companion object {
        const val PREFS_NAME = "session_prefs"
        const val KEY_ROLE = "current_user_role"
    }
}

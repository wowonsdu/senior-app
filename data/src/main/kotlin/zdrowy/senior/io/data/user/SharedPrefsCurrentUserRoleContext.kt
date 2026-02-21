package zdrowy.senior.io.data.user

import android.content.Context
import android.content.SharedPreferences
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import zdrowy.senior.io.domain.user.CurrentUserRoleContext
import zdrowy.senior.io.domain.user.UserRole

class SharedPrefsCurrentUserRoleContext(
    appContext: Context
) : CurrentUserRoleContext {
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val subject = BehaviorSubject.createDefault(readRole())

    override fun getRole(): UserRole? = subject.value

    override fun observeRole(): Observable<UserRole?> = subject.hide()

    override fun setRole(role: UserRole): Completable {
        return Completable.fromAction {
            prefs.edit().putString(KEY_ROLE, role.name).apply()
            subject.onNext(role)
        }
    }

    override fun clearRole(): Completable {
        return Completable.fromAction {
            prefs.edit().remove(KEY_ROLE).apply()
            subject.onNext(null)
        }
    }

    private fun readRole(): UserRole? {
        val raw = prefs.getString(KEY_ROLE, null) ?: return null
        return runCatching { UserRole.valueOf(raw) }.getOrNull()
    }

    private companion object {
        const val PREFS_NAME = "session_prefs"
        const val KEY_ROLE = "current_user_role"
    }
}

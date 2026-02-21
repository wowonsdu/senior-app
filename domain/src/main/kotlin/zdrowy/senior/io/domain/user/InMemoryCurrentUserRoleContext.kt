package zdrowy.senior.io.domain.user

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class InMemoryCurrentUserRoleContext : CurrentUserRoleContext {
    private val subject = BehaviorSubject.createDefault<UserRoleState>(UserRoleState.Missing)

    override fun getRole(): UserRole? {
        val state = subject.value
        return if (state is UserRoleState.Available) state.role else null
    }

    override fun observeRole(): Observable<UserRoleState> = subject.hide()

    override fun setRole(role: UserRole): Completable {
        return Completable.fromAction { subject.onNext(UserRoleState.Available(role)) }
    }

    override fun clearRole(): Completable {
        return Completable.fromAction { subject.onNext(UserRoleState.Missing) }
    }
}

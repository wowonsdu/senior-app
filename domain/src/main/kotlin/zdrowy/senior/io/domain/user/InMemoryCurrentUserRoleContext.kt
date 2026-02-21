package zdrowy.senior.io.domain.user

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class InMemoryCurrentUserRoleContext : CurrentUserRoleContext {
    private val subject = BehaviorSubject.create<UserRole>()

    override fun getRole(): UserRole? = subject.value

    override fun observeRole(): Observable<UserRole> = subject.hide()

    override fun setRole(role: UserRole): Completable {
        return Completable.fromAction { subject.onNext(role) }
    }

    override fun clearRole(): Completable {
        return Completable.fromAction { subject.onComplete() }
    }
}

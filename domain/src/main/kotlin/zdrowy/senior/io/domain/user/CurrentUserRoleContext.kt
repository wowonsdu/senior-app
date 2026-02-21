package zdrowy.senior.io.domain.user

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface CurrentUserRoleContext {
    fun getRole(): UserRole?
    fun observeRole(): Observable<UserRoleState>
    fun setRole(role: UserRole): Completable
    fun clearRole(): Completable
}

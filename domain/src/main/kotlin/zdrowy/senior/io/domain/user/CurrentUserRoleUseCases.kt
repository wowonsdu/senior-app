package zdrowy.senior.io.domain.user

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class SetCurrentUserRoleUseCase(
    private val context: CurrentUserRoleContext
) {
    operator fun invoke(role: UserRole): Completable = context.setRole(role)
}

class ObserveCurrentUserRoleUseCase(
    private val context: CurrentUserRoleContext
) {
    operator fun invoke(): Observable<UserRoleState> = context.observeRole()
}

class GetCurrentUserRoleUseCase(
    private val context: CurrentUserRoleContext
) {
    operator fun invoke(): Single<UserRole> {
        val role = context.getRole()
        return if (role != null) {
            Single.just(role)
        } else {
            Single.error(IllegalStateException("User role not set"))
        }
    }
}

class ClearCurrentUserRoleUseCase(
    private val context: CurrentUserRoleContext
) {
    operator fun invoke(): Completable = context.clearRole()
}

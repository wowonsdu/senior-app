package zdrowy.senior.io.domain.user

import io.reactivex.rxjava3.core.Single

class EnsureCurrentUserRoleLoadedUseCase(
    private val context: CurrentUserRoleContext,
    private val repository: UserProfileRepository
) {
    operator fun invoke(): Single<UserRole> {
        val cached = context.getRole()
        return if (cached != null) {
            Single.just(cached)
        } else {
            repository.getCurrentUserRole()
                .flatMap { role -> context.setRole(role).andThen(Single.just(role)) }
        }
    }
}

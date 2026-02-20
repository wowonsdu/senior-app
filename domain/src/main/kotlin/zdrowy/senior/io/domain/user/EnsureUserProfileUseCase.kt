package zdrowy.senior.io.domain.user

import io.reactivex.rxjava3.core.Completable

class EnsureUserProfileUseCase(
    private val repository: UserProfileRepository
) {
    operator fun invoke(role: UserRole): Completable = repository.ensureCurrentUserProfile(role)
}


package zdrowy.senior.io.domain.auth

import io.reactivex.rxjava3.core.Completable

class SignOutUseCase(
    private val authRepository: AuthRepository
) {
    fun execute(): Completable {
        return authRepository.signOut()
    }
}

package zdrowy.senior.io.domain.auth

import io.reactivex.rxjava3.core.Single

class VerifyOtpUseCase(
    private val authRepository: AuthRepository
) {
    fun execute(verificationId: String, code: String): Single<AuthSession> {
        return authRepository.verifyOtp(verificationId, code)
    }
}

package zdrowy.senior.io.domain.auth

import android.app.Activity
import io.reactivex.rxjava3.core.Single

class SendOtpUseCase(
    private val authRepository: AuthRepository
) {
    fun execute(phoneE164: String, activity: Activity): Single<PhoneVerification> {
        return authRepository.sendOtp(phoneE164, activity)
    }
}

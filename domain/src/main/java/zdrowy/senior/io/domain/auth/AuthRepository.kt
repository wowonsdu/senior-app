package zdrowy.senior.io.domain.auth

import android.app.Activity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface AuthRepository {
    fun sendOtp(phoneE164: String, activity: Activity): Single<PhoneVerification>
    fun verifyOtp(verificationId: String, code: String): Single<AuthSession>
    fun currentSession(): AuthSession?
    fun signOut(): Completable
}

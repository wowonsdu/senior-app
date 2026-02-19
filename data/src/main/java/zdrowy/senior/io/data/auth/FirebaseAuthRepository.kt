package zdrowy.senior.io.data.auth

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.domain.auth.AuthRepository
import zdrowy.senior.io.domain.auth.AuthSession
import zdrowy.senior.io.domain.auth.PhoneVerification
import java.util.concurrent.TimeUnit

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override fun sendOtp(phoneE164: String, activity: Activity): Single<PhoneVerification> {
        return Single.create { emitter ->
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-retrieval can happen; UI still supports manual OTP.
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    if (!emitter.isDisposed) {
                        emitter.tryOnError(exception)
                    }
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    if (!emitter.isDisposed) {
                        emitter.onSuccess(PhoneVerification(verificationId))
                    }
                }
            }

            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneE164)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    override fun verifyOtp(verificationId: String, code: String): Single<AuthSession> {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        return Single.create { emitter ->
            firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener { result ->
                    val user = result.user
                    val uid = user?.uid
                    if (uid == null) {
                        emitter.tryOnError(IllegalStateException("Missing user uid"))
                    } else {
                        emitter.onSuccess(AuthSession(uid, user.phoneNumber))
                    }
                }
                .addOnFailureListener { exception ->
                    emitter.tryOnError(exception)
                }
        }
    }

    override fun currentSession(): AuthSession? {
        val user = firebaseAuth.currentUser ?: return null
        return AuthSession(user.uid, user.phoneNumber)
    }

    override fun signOut(): Completable {
        return Completable.fromAction {
            firebaseAuth.signOut()
        }
    }
}

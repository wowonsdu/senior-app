package zdrowy.senior.io.data.invite

import com.google.firebase.functions.FirebaseFunctions
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.domain.invite.InviteRepository
import zdrowy.senior.io.domain.invite.InviteResolution

class FirebaseInviteRepository(
    private val functions: FirebaseFunctions
) : InviteRepository {

    override fun resolveInvite(code: String, phoneE164: String): Single<InviteResolution> {
        val data = hashMapOf(
            "code" to code,
            "phoneE164" to phoneE164
        )
        return Single.create { emitter ->
            functions
                .getHttpsCallable("resolveInvite")
                .call(data)
                .addOnSuccessListener { result ->
                    val payload = result.data as? Map<*, *>
                    val inviteId = payload?.get("inviteId") as? String
                    if (inviteId.isNullOrBlank()) {
                        emitter.tryOnError(IllegalStateException("Missing inviteId"))
                    } else {
                        emitter.onSuccess(InviteResolution(inviteId))
                    }
                }
                .addOnFailureListener { error ->
                    emitter.tryOnError(error)
                }
        }
    }

    override fun claimInvite(inviteId: String): Completable {
        val data = hashMapOf(
            "inviteId" to inviteId
        )
        return Completable.create { emitter ->
            functions
                .getHttpsCallable("claimInvite")
                .call(data)
                .addOnSuccessListener {
                    emitter.onComplete()
                }
                .addOnFailureListener { error ->
                    emitter.tryOnError(error)
                }
        }
    }
}

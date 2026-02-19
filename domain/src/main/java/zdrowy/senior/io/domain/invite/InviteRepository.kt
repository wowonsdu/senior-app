package zdrowy.senior.io.domain.invite

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface InviteRepository {
    fun resolveInvite(code: String, phoneE164: String): Single<InviteResolution>
    fun claimInvite(inviteId: String): Completable
}

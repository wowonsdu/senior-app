package zdrowy.senior.io.domain.invite

import io.reactivex.rxjava3.core.Single

class ResolveInviteUseCase(
    private val inviteRepository: InviteRepository
) {
    fun execute(code: String, phoneE164: String): Single<InviteResolution> {
        return inviteRepository.resolveInvite(code, phoneE164)
    }
}

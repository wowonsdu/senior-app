package zdrowy.senior.io.domain.invite

import io.reactivex.rxjava3.core.Completable

class ClaimInviteUseCase(
    private val inviteRepository: InviteRepository
) {
    fun execute(inviteId: String): Completable {
        return inviteRepository.claimInvite(inviteId)
    }
}

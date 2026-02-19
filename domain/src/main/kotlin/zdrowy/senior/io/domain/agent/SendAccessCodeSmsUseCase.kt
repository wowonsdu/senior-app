package zdrowy.senior.io.domain.agent

import io.reactivex.rxjava3.core.Completable

class SendAccessCodeSmsUseCase(
    private val repository: NotificationRepository
) {
    operator fun invoke(agentId: String, accessCode: AccessCode): Completable =
        repository.sendAccessCodeSms(agentId, accessCode)
}

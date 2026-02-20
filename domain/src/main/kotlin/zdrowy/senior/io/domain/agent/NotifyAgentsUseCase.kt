package zdrowy.senior.io.domain.agent

import io.reactivex.rxjava3.core.Completable

class NotifyAgentsUseCase(
    private val repository: NotificationRepository
) {
    operator fun invoke(agentIds: List<String>, message: String): Completable =
        repository.notifyAgents(agentIds, message)
}

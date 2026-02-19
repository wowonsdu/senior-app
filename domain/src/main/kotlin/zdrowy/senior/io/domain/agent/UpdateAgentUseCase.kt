package zdrowy.senior.io.domain.agent

import io.reactivex.rxjava3.core.Completable

class UpdateAgentUseCase(
    private val repository: AgentRepository
) {
    operator fun invoke(agentId: String, update: AgentUpdate): Completable =
        repository.updateAgent(agentId, update)
}

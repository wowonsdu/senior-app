package zdrowy.senior.io.domain.agent

import io.reactivex.rxjava3.core.Completable

class RemoveAgentUseCase(
    private val repository: AgentRepository
) {
    operator fun invoke(agentId: String): Completable = repository.removeAgent(agentId)
}

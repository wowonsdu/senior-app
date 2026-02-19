package zdrowy.senior.io.domain.agent

import io.reactivex.rxjava3.core.Single

class ListAgentsUseCase(
    private val repository: AgentRepository
) {
    operator fun invoke(): Single<List<Agent>> = repository.listAgents()
}

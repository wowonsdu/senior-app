package zdrowy.senior.io.domain.agent

import io.reactivex.rxjava3.core.Observable

class ObserveAgentsUseCase(
    private val repository: AgentRepository
) {
    operator fun invoke(): Observable<List<Agent>> = repository.observeAgents()
}

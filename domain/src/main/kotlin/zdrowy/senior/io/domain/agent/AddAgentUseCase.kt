package zdrowy.senior.io.domain.agent

import io.reactivex.rxjava3.core.Single

class AddAgentUseCase(
    private val repository: AgentRepository
) {
    operator fun invoke(draft: AgentDraft): Single<String> = repository.addAgent(draft)
}

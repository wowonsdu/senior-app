package zdrowy.senior.io.domain.agent

import io.reactivex.rxjava3.core.Single

class GenerateAccessCodeForAgentUseCase(
    private val repository: AccessCodeRepository
) {
    operator fun invoke(agentId: String, ttlSeconds: Long): Single<AccessCode> =
        repository.generateAccessCodeForAgent(agentId, ttlSeconds)
}

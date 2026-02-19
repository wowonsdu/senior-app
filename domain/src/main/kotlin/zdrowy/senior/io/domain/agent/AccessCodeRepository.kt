package zdrowy.senior.io.domain.agent

import io.reactivex.rxjava3.core.Single

interface AccessCodeRepository {
    fun generateAccessCodeForAgent(agentId: String, ttlSeconds: Long): Single<AccessCode>
}

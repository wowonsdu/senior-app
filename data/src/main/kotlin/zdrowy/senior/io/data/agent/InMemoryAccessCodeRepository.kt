package zdrowy.senior.io.data.agent

import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.domain.agent.AccessCode
import zdrowy.senior.io.domain.agent.AccessCodeRepository
import kotlin.random.Random

class InMemoryAccessCodeRepository : AccessCodeRepository {
    override fun generateAccessCodeForAgent(agentId: String, ttlSeconds: Long): Single<AccessCode> {
        val code = (100000..999999).random(Random(System.currentTimeMillis())).toString()
        val expiresAt = System.currentTimeMillis() + ttlSeconds * 1000
        return Single.just(AccessCode(code = code, expiresAt = expiresAt))
    }
}

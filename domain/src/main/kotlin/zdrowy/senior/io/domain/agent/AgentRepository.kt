package zdrowy.senior.io.domain.agent

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface AgentRepository {
    fun addAgent(draft: AgentDraft): Single<String>
    fun addDoctor(draft: DoctorDraft): Single<String>
    fun updateAgent(agentId: String, update: AgentUpdate): Completable
    fun removeAgent(agentId: String): Completable
    fun listAgents(): Single<List<Agent>>
    fun observeAgents(): Observable<List<Agent>>
}

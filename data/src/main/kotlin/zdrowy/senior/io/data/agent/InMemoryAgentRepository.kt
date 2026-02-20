package zdrowy.senior.io.data.agent

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import zdrowy.senior.io.domain.agent.Agent
import zdrowy.senior.io.domain.agent.AgentDraft
import zdrowy.senior.io.domain.agent.AgentRepository
import zdrowy.senior.io.domain.agent.AgentRole
import zdrowy.senior.io.domain.agent.AgentUpdate
import zdrowy.senior.io.domain.agent.DoctorDraft
import java.util.UUID

class InMemoryAgentRepository : AgentRepository {
    private val agents = mutableListOf<Agent>()
    private val agentsSubject = BehaviorSubject.create<List<Agent>>()

    init {
        seedData()
        agentsSubject.onNext(agents.toList())
    }

    override fun addAgent(draft: AgentDraft): Single<String> {
        val id = UUID.randomUUID().toString()
        agents.add(
            Agent(
                id = id,
                fullName = draft.fullName,
                role = AgentRole.CAREGIVER,
                phone = draft.phone,
                email = draft.email,
                specialization = null
            )
        )
        agentsSubject.onNext(agents.toList())
        return Single.just(id)
    }

    override fun addDoctor(draft: DoctorDraft): Single<String> {
        val id = UUID.randomUUID().toString()
        agents.add(
            Agent(
                id = id,
                fullName = draft.fullName,
                role = AgentRole.DOCTOR,
                phone = draft.phone,
                email = draft.email,
                specialization = draft.specialization
            )
        )
        agentsSubject.onNext(agents.toList())
        return Single.just(id)
    }

    override fun updateAgent(agentId: String, update: AgentUpdate): Completable {
        val index = agents.indexOfFirst { it.id == agentId }
        if (index >= 0) {
            val current = agents[index]
            agents[index] = current.copy(
                fullName = update.fullName ?: current.fullName,
                phone = update.phone ?: current.phone,
                email = update.email ?: current.email,
                specialization = update.specialization ?: current.specialization
            )
        }
        agentsSubject.onNext(agents.toList())
        return Completable.complete()
    }

    override fun removeAgent(agentId: String): Completable {
        agents.removeAll { it.id == agentId }
        agentsSubject.onNext(agents.toList())
        return Completable.complete()
    }

    override fun listAgents(): Single<List<Agent>> = Single.just(agents.toList())

    override fun observeAgents(): Observable<List<Agent>> = agentsSubject.hide()

    private fun seedData() {
        agents += Agent(
            id = UUID.randomUUID().toString(),
            fullName = "Maria Nowak",
            role = AgentRole.CAREGIVER,
            phone = "+48 600 000 111",
            email = "maria.nowak@example.com",
            specialization = null
        )
        agents += Agent(
            id = UUID.randomUUID().toString(),
            fullName = "Jan Kowalski",
            role = AgentRole.CAREGIVER,
            phone = "+48 600 000 222",
            email = "jan.kowalski@example.com",
            specialization = null
        )
        agents += Agent(
            id = UUID.randomUUID().toString(),
            fullName = "Dr Anna Zielinska",
            role = AgentRole.DOCTOR,
            phone = "+48 600 000 333",
            email = "anna.zielinska@example.com",
            specialization = "Kardiolog"
        )
    }
}

package zdrowy.senior.io.data.agent

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.data.firestore.toCompletable
import zdrowy.senior.io.data.firestore.toSingle
import zdrowy.senior.io.domain.agent.Agent
import zdrowy.senior.io.domain.agent.AgentDraft
import zdrowy.senior.io.domain.agent.AgentRepository
import zdrowy.senior.io.domain.agent.AgentRole
import zdrowy.senior.io.domain.agent.AgentUpdate
import zdrowy.senior.io.domain.agent.DoctorDraft
import java.util.Locale

class FirestoreAgentRepository : AgentRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun addAgent(draft: AgentDraft): Single<String> {
        val uid = requireUid()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.CONTACTS)

        return col.add(
            mapOf(
                "fullName" to draft.fullName,
                "role" to AgentRole.CAREGIVER.name,
                "phone" to draft.phone,
                "email" to draft.email,
                "specialization" to null,
                "linkedUid" to null,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).toSingle().map { it.id }
    }

    override fun addDoctor(draft: DoctorDraft): Single<String> {
        val uid = requireUid()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.CONTACTS)

        return col.add(
            mapOf(
                "fullName" to draft.fullName,
                "role" to AgentRole.DOCTOR.name,
                "phone" to draft.phone,
                "email" to draft.email,
                "specialization" to draft.specialization,
                "linkedUid" to null,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).toSingle().map { it.id }
    }

    override fun updateAgent(agentId: String, update: AgentUpdate): Completable {
        val uid = requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.CONTACTS)
            .document(agentId)

        val payload = mutableMapOf<String, Any>(
            "updatedAt" to FieldValue.serverTimestamp()
        )
        update.fullName?.let { payload["fullName"] = it }
        update.phone?.let { payload["phone"] = it }
        update.email?.let { payload["email"] = it }
        update.specialization?.let { payload["specialization"] = it }

        return doc.set(payload, SetOptions.merge()).toCompletable()
    }

    override fun removeAgent(agentId: String): Completable {
        val uid = requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.CONTACTS)
            .document(agentId)
        return doc.delete().toCompletable()
    }

    override fun listAgents(): Single<List<Agent>> {
        val uid = requireUid()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.CONTACTS)

        return col.get()
            .toSingle()
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    val roleRaw = doc.getString("role").orEmpty()
                    val role = runCatching { AgentRole.valueOf(roleRaw) }.getOrDefault(AgentRole.CAREGIVER)
                    Agent(
                        id = doc.id,
                        fullName = doc.getString("fullName").orEmpty(),
                        role = role,
                        phone = doc.getString("phone").orEmpty(),
                        email = doc.getString("email").orEmpty(),
                        specialization = doc.getString("specialization")
                    )
                }.sortedBy { it.fullName.lowercase(Locale.ROOT) }
            }
    }

    private fun requireUid(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")
    }
}


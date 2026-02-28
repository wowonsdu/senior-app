package zdrowy.senior.io.data.agent

import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.rxjava3.core.Observable
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.domain.agent.Agent
import zdrowy.senior.io.domain.agent.AgentRole
import zdrowy.senior.io.domain.agent.DoctorByUidRepository

class FirestoreDoctorByUidRepository : DoctorByUidRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun observeDoctors(patientUid: String): Observable<List<Agent>> {
        return Observable.create { emitter ->
            val col = firestore.collection(FirestorePaths.USERS)
                .document(patientUid)
                .collection(FirestorePaths.CONTACTS)

            val registration = col.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!emitter.isDisposed) emitter.onError(error)
                    return@addSnapshotListener
                }
                val doctors = snapshot?.documents.orEmpty()
                    .mapNotNull { doc ->
                        val roleRaw = doc.getString("role").orEmpty()
                        val role = runCatching { AgentRole.valueOf(roleRaw) }
                            .getOrDefault(AgentRole.CAREGIVER)
                        if (role != AgentRole.DOCTOR) return@mapNotNull null
                        Agent(
                            id = doc.id,
                            fullName = doc.getString("fullName").orEmpty(),
                            role = role,
                            phone = doc.getString("phone").orEmpty(),
                            email = doc.getString("email").orEmpty(),
                            specialization = doc.getString("specialization")
                        )
                    }
                    .sortedBy { it.fullName.lowercase() }

                if (!emitter.isDisposed) emitter.onNext(doctors)
            }
            emitter.setCancellable { registration.remove() }
        }
    }
}

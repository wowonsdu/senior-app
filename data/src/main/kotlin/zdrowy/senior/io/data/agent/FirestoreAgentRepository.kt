package zdrowy.senior.io.data.agent

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.data.firestore.PatientUidProvider
import zdrowy.senior.io.data.firestore.toCompletable
import zdrowy.senior.io.data.firestore.toSingle
import zdrowy.senior.io.domain.agent.Agent
import zdrowy.senior.io.domain.agent.AgentDraft
import zdrowy.senior.io.domain.agent.AgentRepository
import zdrowy.senior.io.domain.agent.AgentRole
import zdrowy.senior.io.domain.agent.AgentUpdate
import zdrowy.senior.io.domain.agent.DoctorDraft
import zdrowy.senior.io.domain.settings.PersonalData
import java.util.Locale

class FirestoreAgentRepository(
    private val uidProvider: PatientUidProvider
) : AgentRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

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
            .map { snapshot -> snapshot.documents.map { it.toAgent() }.sortedBy { it.fullName.lowercase(Locale.ROOT) } }
    }

    override fun observeAgents(): Observable<List<Agent>> {
        return Observable.create { emitter ->
            val uid = try {
                requireUid()
            } catch (error: Throwable) {
                emitter.onError(error)
                return@create
            }
            val col = firestore.collection(FirestorePaths.USERS)
                .document(uid)
                .collection(FirestorePaths.CONTACTS)

            var innerDisposable: Disposable? = null

            val registration = col.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!emitter.isDisposed) emitter.onError(error)
                    return@addSnapshotListener
                }
                val records = snapshot?.documents.orEmpty().map { it.toContactRecord() }
                innerDisposable?.dispose()
                if (records.isEmpty()) {
                    if (!emitter.isDisposed) emitter.onNext(emptyList())
                    return@addSnapshotListener
                }

                val observers = records.map { record ->
                    val base = record.agent
                    val linkedUid = record.linkedUid
                    if (base.role == AgentRole.CAREGIVER && linkedUid.isNotBlank()) {
                        observePersonalData(linkedUid)
                            .map { data -> mergeWithPersonalData(base, data) }
                            .onErrorReturnItem(base)
                    } else {
                        Observable.just(base)
                    }
                }

                innerDisposable = Observable.combineLatest(observers) { items ->
                    items.map { it as Agent }
                }
                    .map { items -> items.sortedBy { it.fullName.lowercase(Locale.ROOT) } }
                    .subscribe({ items ->
                        if (!emitter.isDisposed) emitter.onNext(items)
                    }, { err ->
                        if (!emitter.isDisposed) emitter.onError(err)
                    })
            }
            emitter.setCancellable {
                registration.remove()
                innerDisposable?.dispose()
            }
        }
    }

    private data class ContactRecord(
        val agent: Agent,
        val linkedUid: String
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toContactRecord(): ContactRecord {
        val roleRaw = getString("role").orEmpty()
        val role = runCatching { AgentRole.valueOf(roleRaw) }.getOrDefault(AgentRole.CAREGIVER)
        val linkedUid = getString("linkedUid").orEmpty()
        val agent = Agent(
            id = id,
            fullName = getString("fullName").orEmpty(),
            role = role,
            phone = getString("phone").orEmpty(),
            email = getString("email").orEmpty(),
            specialization = getString("specialization")
        )
        return ContactRecord(agent = agent, linkedUid = linkedUid)
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toAgent(): Agent =
        toContactRecord().agent

    private fun observePersonalData(uid: String): Observable<PersonalData> {
        val fallback = PersonalData(
            firstName = "",
            lastName = "",
            pesel = "",
            phoneNumber = "",
            email = "",
            address = ""
        )

        return Observable.create { emitter ->
            val doc = firestore.collection(FirestorePaths.USERS)
                .document(uid)
                .collection(FirestorePaths.SETTINGS)
                .document(FirestorePaths.PERSONAL_DATA)

            val registration = doc.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!emitter.isDisposed) emitter.onError(error)
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) {
                    if (!emitter.isDisposed) emitter.onNext(fallback)
                    return@addSnapshotListener
                }
                val data = PersonalData(
                    firstName = snapshot.getString("firstName").orEmpty(),
                    lastName = snapshot.getString("lastName").orEmpty(),
                    pesel = snapshot.getString("pesel").orEmpty(),
                    phoneNumber = snapshot.getString("phoneNumber").orEmpty(),
                    email = snapshot.getString("email").orEmpty(),
                    address = snapshot.getString("address").orEmpty()
                )
                if (!emitter.isDisposed) emitter.onNext(data)
            }
            emitter.setCancellable { registration.remove() }
        }
    }

    private fun mergeWithPersonalData(base: Agent, data: PersonalData): Agent {
        val firstName = data.firstName.trim()
        val lastName = data.lastName.trim()
        val fullName = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
        val phone = data.phoneNumber.trim()
        val resolvedName = if (fullName.isBlank()) base.fullName else fullName
        val resolvedPhone = if (phone.isBlank()) base.phone else phone
        return base.copy(fullName = resolvedName, phone = resolvedPhone)
    }

    private fun requireUid(): String = uidProvider.requirePatientUid()
}

package zdrowy.senior.io.data.settings

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.data.firestore.toCompletable
import zdrowy.senior.io.data.firestore.toSingle
import zdrowy.senior.io.domain.settings.Disease
import zdrowy.senior.io.domain.settings.DiseaseDraft
import zdrowy.senior.io.domain.settings.DiseaseUpdate
import zdrowy.senior.io.domain.settings.Medication
import zdrowy.senior.io.domain.settings.MedicationDraft
import zdrowy.senior.io.domain.settings.MedicationUpdate
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.domain.settings.SettingsRepository

class FirestoreSettingsRepository : SettingsRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun getPersonalData(): Single<PersonalData> {
        val uid = requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.SETTINGS)
            .document(FirestorePaths.PERSONAL_DATA)

        val fallback = PersonalData(
            firstName = "",
            lastName = "",
            pesel = "",
            phoneNumber = "",
            email = "",
            address = ""
        )

        return doc.get()
            .toSingle()
            .map { snapshot ->
                if (!snapshot.exists()) return@map fallback
                PersonalData(
                    firstName = snapshot.getString("firstName").orEmpty(),
                    lastName = snapshot.getString("lastName").orEmpty(),
                    pesel = snapshot.getString("pesel").orEmpty(),
                    phoneNumber = snapshot.getString("phoneNumber").orEmpty(),
                    email = snapshot.getString("email").orEmpty(),
                    address = snapshot.getString("address").orEmpty()
                )
            }
    }

    override fun observePersonalData(): Observable<PersonalData> {
        val fallback = PersonalData(
            firstName = "",
            lastName = "",
            pesel = "",
            phoneNumber = "",
            email = "",
            address = ""
        )

        return Observable.create { emitter ->
            val uid = try {
                requireUid()
            } catch (error: Throwable) {
                emitter.onError(error)
                return@create
            }
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

    override fun upsertPersonalData(data: PersonalData): Completable {
        val uid = requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.SETTINGS)
            .document(FirestorePaths.PERSONAL_DATA)

        return doc.set(
            mapOf(
                "firstName" to data.firstName,
                "lastName" to data.lastName,
                "pesel" to data.pesel,
                "phoneNumber" to data.phoneNumber,
                "email" to data.email,
                "address" to data.address,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).toCompletable()
    }

    override fun addDisease(draft: DiseaseDraft): Single<String> {
        val uid = requireUid()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.DISEASES)

        return col.add(
            mapOf(
                "name" to draft.name,
                "severity" to draft.severity,
                "notes" to draft.notes,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).toSingle().map { it.id }
    }

    override fun updateDisease(id: String, update: DiseaseUpdate): Completable {
        val uid = requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.DISEASES)
            .document(id)

        val payload = mutableMapOf<String, Any>(
            "updatedAt" to FieldValue.serverTimestamp()
        )
        update.name?.let { payload["name"] = it }
        update.severity?.let { payload["severity"] = it }
        update.notes?.let { payload["notes"] = it }

        return doc.set(payload, SetOptions.merge()).toCompletable()
    }

    override fun removeDisease(id: String): Completable {
        val uid = requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.DISEASES)
            .document(id)
        return doc.delete().toCompletable()
    }

    override fun listDiseases(): Single<List<Disease>> {
        val uid = requireUid()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.DISEASES)
        return col.get()
            .toSingle()
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    Disease(
                        id = doc.id,
                        name = doc.getString("name").orEmpty(),
                        severity = doc.getString("severity").orEmpty(),
                        notes = doc.getString("notes").orEmpty()
                    )
                }.sortedBy { it.name.lowercase() }
            }
    }

    override fun observeDiseases(): Observable<List<Disease>> {
        return Observable.create { emitter ->
            val uid = try {
                requireUid()
            } catch (error: Throwable) {
                emitter.onError(error)
                return@create
            }
            val col = firestore.collection(FirestorePaths.USERS)
                .document(uid)
                .collection(FirestorePaths.DISEASES)

            val registration = col.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!emitter.isDisposed) emitter.onError(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().map { doc ->
                    Disease(
                        id = doc.id,
                        name = doc.getString("name").orEmpty(),
                        severity = doc.getString("severity").orEmpty(),
                        notes = doc.getString("notes").orEmpty()
                    )
                }.sortedBy { it.name.lowercase() }
                if (!emitter.isDisposed) emitter.onNext(items)
            }
            emitter.setCancellable { registration.remove() }
        }
    }

    override fun addMedication(draft: MedicationDraft): Single<String> {
        val uid = requireUid()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.MEDICATIONS)

        return col.add(
            mapOf(
                "name" to draft.name,
                "dosage" to draft.dosage,
                "schedule" to draft.schedule,
                "notificationsEnabled" to draft.notificationsEnabled,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).toSingle().map { it.id }
    }

    override fun updateMedication(id: String, update: MedicationUpdate): Completable {
        val uid = requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.MEDICATIONS)
            .document(id)

        val payload = mutableMapOf<String, Any>(
            "updatedAt" to FieldValue.serverTimestamp()
        )
        update.name?.let { payload["name"] = it }
        update.dosage?.let { payload["dosage"] = it }
        update.schedule?.let { payload["schedule"] = it }
        update.notificationsEnabled?.let { payload["notificationsEnabled"] = it }

        return doc.set(payload, SetOptions.merge()).toCompletable()
    }

    override fun removeMedication(id: String): Completable {
        val uid = requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.MEDICATIONS)
            .document(id)
        return doc.delete().toCompletable()
    }

    override fun listMedications(): Single<List<Medication>> {
        val uid = requireUid()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.MEDICATIONS)
        return col.get()
            .toSingle()
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    Medication(
                        id = doc.id,
                        name = doc.getString("name").orEmpty(),
                        dosage = doc.getString("dosage").orEmpty(),
                        schedule = doc.getString("schedule").orEmpty(),
                        notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: false
                    )
                }.sortedBy { it.name.lowercase() }
            }
    }

    override fun observeMedications(): Observable<List<Medication>> {
        return Observable.create { emitter ->
            val uid = try {
                requireUid()
            } catch (error: Throwable) {
                emitter.onError(error)
                return@create
            }
            val col = firestore.collection(FirestorePaths.USERS)
                .document(uid)
                .collection(FirestorePaths.MEDICATIONS)

            val registration = col.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!emitter.isDisposed) emitter.onError(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().map { doc ->
                    Medication(
                        id = doc.id,
                        name = doc.getString("name").orEmpty(),
                        dosage = doc.getString("dosage").orEmpty(),
                        schedule = doc.getString("schedule").orEmpty(),
                        notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: false
                    )
                }.sortedBy { it.name.lowercase() }
                if (!emitter.isDisposed) emitter.onNext(items)
            }
            emitter.setCancellable { registration.remove() }
        }
    }

    override fun toggleMedicationNotifications(id: String, enabled: Boolean): Completable {
        val uid = requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.MEDICATIONS)
            .document(id)
        return doc.set(
            mapOf(
                "notificationsEnabled" to enabled,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).toCompletable()
    }

    private fun requireUid(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")
    }
}

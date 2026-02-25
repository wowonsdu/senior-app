package zdrowy.senior.io.data.carelink

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.data.firestore.toCompletable
import zdrowy.senior.io.data.firestore.toSingle
import zdrowy.senior.io.domain.carelink.CareLink
import zdrowy.senior.io.domain.carelink.CareLinkCode
import zdrowy.senior.io.domain.carelink.CareLinkCodeInfo
import zdrowy.senior.io.domain.carelink.CareLinkCodeType
import zdrowy.senior.io.domain.carelink.CareLinkDraft
import zdrowy.senior.io.domain.carelink.CareLinkRepository
import zdrowy.senior.io.domain.carelink.CareLinkStatus
import zdrowy.senior.io.domain.agent.AgentRole
import zdrowy.senior.io.domain.user.CurrentUserRoleContext
import zdrowy.senior.io.domain.user.UserRole
import kotlin.random.Random

class FirestoreCareLinkRepository(
    private val roleContext: CurrentUserRoleContext
) : CareLinkRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val random: Random = Random(System.currentTimeMillis())

    override fun observeCareLinks(): Observable<List<CareLink>> {
        return Observable.create { emitter ->
            val uid = try {
                requireUid()
            } catch (error: Throwable) {
                emitter.onError(error)
                return@create
            }
            val role = try {
                requireRole()
            } catch (error: Throwable) {
                emitter.onError(error)
                return@create
            }

            val col = firestore.collection(FirestorePaths.CARE_LINKS)
            val query = if (role == UserRole.CAREGIVER) {
                col.whereEqualTo("caregiverUid", uid)
            } else {
                col.whereEqualTo("patientUid", uid)
            }

            val registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!emitter.isDisposed) emitter.onError(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().map { doc ->
                    val patientUid = doc.getString("patientUid").orEmpty()
                    val caregiverUid = doc.getString("caregiverUid").orEmpty()
                    val statusRaw = doc.getString("status").orEmpty()
                    val status = runCatching { CareLinkStatus.valueOf(statusRaw) }
                        .getOrDefault(CareLinkStatus.ACTIVE)
                    CareLink(patientUid = patientUid, caregiverUid = caregiverUid, status = status)
                }
                if (!emitter.isDisposed) emitter.onNext(items)
            }
            emitter.setCancellable { registration.remove() }
        }
    }

    override fun generateLinkCode(
        type: CareLinkCodeType,
        ttlSeconds: Long,
        draft: CareLinkDraft?
    ): Single<CareLinkCode> {
        val role = requireRole()
        if (type == CareLinkCodeType.PATIENT_TO_CAREGIVER) {
            return Single.error(IllegalStateException("Flow PACJENT->OPIEKUN jest wylaczony"))
        }
        if (type == CareLinkCodeType.CAREGIVER_TO_PATIENT && role != UserRole.CAREGIVER) {
            return Single.error(IllegalStateException("Kod opiekuna moze byc generowany tylko przez opiekuna"))
        }
        val ownerPhone = requireAuthPhone()
        val phoneNumberE164 = draft?.phoneNumber?.trim().orEmpty()
        if (type == CareLinkCodeType.CAREGIVER_TO_PATIENT && phoneNumberE164.isBlank()) {
            return Single.error(IllegalStateException("Telefon pacjenta jest wymagany"))
        }
        if (phoneNumberE164 == ownerPhone) {
            return Single.error(IllegalStateException("Kod nie moze byc generowany na wlasny numer"))
        }
        val uid = requireUid()
        val expiresAtMs = System.currentTimeMillis() + ttlSeconds * 1000
        val code = random.nextInt(100000, 1000000).toString()
        val accessCode = CareLinkCode(code = code, expiresAt = expiresAtMs, type = type)
        val doc = firestore.collection(FirestorePaths.ACCESS_CODES).document(phoneNumberE164)

        val payload = mutableMapOf<String, Any>(
            "type" to type.name,
            "ownerUid" to uid,
            "phoneNumberE164" to phoneNumberE164,
            "code" to code,
            "expiresAtMs" to expiresAtMs,
            "createdAt" to FieldValue.serverTimestamp()
        )
        when (type) {
            CareLinkCodeType.PATIENT_TO_CAREGIVER -> payload["patientUid"] = uid
            CareLinkCodeType.CAREGIVER_TO_PATIENT -> payload["caregiverUid"] = uid
        }

        val draftPayload = mutableMapOf<String, Any>()
        if (draft != null && type == CareLinkCodeType.CAREGIVER_TO_PATIENT) {
            if (draft.firstName.isNotBlank()) draftPayload["draftFirstName"] = draft.firstName
            if (draft.lastName.isNotBlank()) draftPayload["draftLastName"] = draft.lastName
            if (draft.pesel.isNotBlank()) draftPayload["draftPesel"] = draft.pesel
            if (draft.address.isNotBlank()) draftPayload["draftAddress"] = draft.address
            draftPayload["phoneNumberE164"] = phoneNumberE164
        }

        return firestore.runTransaction { tx ->
            tx.set(doc, payload)
            if (draftPayload.isNotEmpty()) {
                val draftDoc = firestore.collection(FirestorePaths.ACCESS_CODE_DRAFTS)
                    .document(phoneNumberE164)
                tx.set(draftDoc, draftPayload)
            }
            true
        }
            .toSingle()
            .map { accessCode }
    }

    override fun getLinkCodeInfo(code: String): Single<CareLinkCodeInfo> {
        val phoneNumberE164 = requireAuthPhone()
        val doc = firestore.collection(FirestorePaths.ACCESS_CODES).document(phoneNumberE164)
        return doc.get()
            .toSingle()
            .map { snapshot ->
                if (!snapshot.exists()) throw InvalidCodeException("Brak kodu")

                val expiresAtMs = (snapshot.get("expiresAtMs") as? Number)?.toLong()
                if (expiresAtMs != null && System.currentTimeMillis() > expiresAtMs) {
                    throw InvalidCodeException("Kod wygasl")
                }

                val storedCode = snapshot.getString("code").orEmpty()
                if (storedCode != code) {
                    throw InvalidCodeException("Nieprawidlowy kod")
                }

                val typeRaw = snapshot.getString("type").orEmpty()
                val type = runCatching { CareLinkCodeType.valueOf(typeRaw) }
                    .getOrElse { throw InvalidCodeException("Nieprawidlowy typ kodu") }

                CareLinkCodeInfo(
                    code = code,
                    type = type,
                    expiresAtMs = expiresAtMs,
                    phoneNumberE164 = phoneNumberE164
                )
            }
            .onErrorResumeNext { error ->
                if (error is FirebaseFirestoreException
                    && error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
                ) {
                    Single.error(InvalidCodeException("Kod dla innego numeru telefonu"))
                } else {
                    Single.error(error)
                }
            }
    }

    override fun consumeLinkCode(code: String): Single<CareLink> {
        val uid = requireUid()
        val phoneNumberE164 = requireAuthPhone()
        val doc = firestore.collection(FirestorePaths.ACCESS_CODES).document(phoneNumberE164)

        return firestore.runTransaction { tx ->
            val snapshot = tx.get(doc)
            if (!snapshot.exists()) throw InvalidCodeException("Brak kodu")

            val expiresAtMs = (snapshot.get("expiresAtMs") as? Number)?.toLong()
            if (expiresAtMs != null && System.currentTimeMillis() > expiresAtMs) {
                throw InvalidCodeException("Kod wygasl")
            }

            val storedCode = snapshot.getString("code").orEmpty()
            if (storedCode != code) {
                throw InvalidCodeException("Nieprawidlowy kod")
            }

            val typeRaw = snapshot.getString("type").orEmpty()
            val type = runCatching { CareLinkCodeType.valueOf(typeRaw) }
                .getOrElse { throw InvalidCodeException("Nieprawidlowy typ kodu") }

            val role = requireRole()
            if (type == CareLinkCodeType.PATIENT_TO_CAREGIVER && role != UserRole.CAREGIVER) {
                throw InvalidCodeException("Kod pacjenta moze byc uzyty tylko przez opiekuna")
            }
            if (type == CareLinkCodeType.CAREGIVER_TO_PATIENT && role != UserRole.PATIENT) {
                throw InvalidCodeException("Kod opiekuna moze byc uzyty tylko przez pacjenta")
            }

            var draftFirstName = snapshot.getString("draftFirstName").orEmpty()
            var draftLastName = snapshot.getString("draftLastName").orEmpty()
            var draftPesel = snapshot.getString("draftPesel").orEmpty()
            val draftPhone = snapshot.getString("phoneNumberE164").orEmpty()
            var draftAddress = snapshot.getString("draftAddress").orEmpty()

            val (patientUid, caregiverUid) = when (type) {
                CareLinkCodeType.PATIENT_TO_CAREGIVER -> {
                    val patientUid = snapshot.getString("patientUid").orEmpty()
                    if (patientUid.isBlank()) throw InvalidCodeException("Brak pacjenta w kodzie")
                    if (patientUid == uid) throw InvalidCodeException("Kod pacjenta nie moze byc uzyty przez pacjenta")
                    patientUid to uid
                }
                CareLinkCodeType.CAREGIVER_TO_PATIENT -> {
                    val caregiverUid = snapshot.getString("caregiverUid").orEmpty()
                    if (caregiverUid.isBlank()) throw InvalidCodeException("Brak opiekuna w kodzie")
                    if (caregiverUid == uid) throw InvalidCodeException("Kod opiekuna nie moze byc uzyty przez opiekuna")
                    uid to caregiverUid
                }
            }

            if (type == CareLinkCodeType.CAREGIVER_TO_PATIENT) {
                val draftDoc = firestore.collection(FirestorePaths.ACCESS_CODE_DRAFTS)
                    .document(phoneNumberE164)
                val draftSnapshot = tx.get(draftDoc)
                if (draftSnapshot.exists()) {
                    draftFirstName = draftSnapshot.getString("draftFirstName").orEmpty()
                    draftLastName = draftSnapshot.getString("draftLastName").orEmpty()
                    draftPesel = draftSnapshot.getString("draftPesel").orEmpty()
                    draftAddress = draftSnapshot.getString("draftAddress").orEmpty()
                }
            }

            val draft = CareLinkDraft(
                firstName = draftFirstName,
                lastName = draftLastName,
                pesel = draftPesel,
                phoneNumber = draftPhone,
                address = draftAddress
            )

            val linkId = "${patientUid}_${caregiverUid}"
            val linkDoc = firestore.collection(FirestorePaths.CARE_LINKS).document(linkId)
            val linkSnapshot = tx.get(linkDoc)
            if (!linkSnapshot.exists()) {
                tx.set(
                    linkDoc,
                    mapOf(
                        "patientUid" to patientUid,
                        "caregiverUid" to caregiverUid,
                        "status" to CareLinkStatus.ACTIVE.name,
                        "createdByUid" to uid,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            if (type == CareLinkCodeType.CAREGIVER_TO_PATIENT) {
                val draftPayload = mutableMapOf<String, Any>()
                if (draft.firstName.isNotBlank()) draftPayload["firstName"] = draft.firstName
                if (draft.lastName.isNotBlank()) draftPayload["lastName"] = draft.lastName
                if (draft.pesel.isNotBlank()) draftPayload["pesel"] = draft.pesel
                if (draft.phoneNumber.isNotBlank()) draftPayload["phoneNumber"] = draft.phoneNumber
                if (draft.address.isNotBlank()) draftPayload["address"] = draft.address
                if (draftPayload.isNotEmpty()) {
                    draftPayload["updatedAt"] = FieldValue.serverTimestamp()
                    val personalDoc = firestore.collection(FirestorePaths.USERS)
                        .document(patientUid)
                        .collection(FirestorePaths.SETTINGS)
                        .document(FirestorePaths.PERSONAL_DATA)
                    tx.set(personalDoc, draftPayload, SetOptions.merge())
                }
            }

            tx.delete(doc)
            if (type == CareLinkCodeType.CAREGIVER_TO_PATIENT) {
                val draftDoc = firestore.collection(FirestorePaths.ACCESS_CODE_DRAFTS)
                    .document(phoneNumberE164)
                tx.delete(draftDoc)
            }
            CareLink(patientUid = patientUid, caregiverUid = caregiverUid, status = CareLinkStatus.ACTIVE)
        }
            .toSingle()
            .onErrorResumeNext { error ->
                if (error is FirebaseFirestoreException
                    && error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
                ) {
                    Single.error(InvalidCodeException("Kod dla innego numeru telefonu"))
                } else {
                    Single.error(error)
                }
            }
    }

    override fun ensureCaregiverContact(caregiverUid: String): Completable {
        if (caregiverUid.isBlank()) return Completable.complete()
        val uid = requireUid()
        val doc = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.CONTACTS)
            .document(caregiverUid)
        return doc.set(
            mapOf(
                "fullName" to "Opiekun",
                "role" to AgentRole.CAREGIVER.name,
                "phone" to "",
                "email" to "",
                "specialization" to null,
                "linkedUid" to caregiverUid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).toCompletable()
    }

    override fun removeCareLink(patientUid: String, caregiverUid: String): Completable {
        if (patientUid.isBlank() || caregiverUid.isBlank()) {
            return Completable.error(IllegalStateException("Brak identyfikatorow powiazania"))
        }
        val linkId = "${patientUid}_${caregiverUid}"
        val doc = firestore.collection(FirestorePaths.CARE_LINKS).document(linkId)
        return doc.delete().toCompletable()
    }

    private fun requireUid(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")
    }

    private fun requireRole(): UserRole {
        return roleContext.getRole() ?: throw IllegalStateException("User role not set")
    }

    private fun requireAuthPhone(): String {
        return auth.currentUser?.phoneNumber?.trim()
            ?: throw IllegalStateException("Brak zweryfikowanego numeru telefonu")
    }


    private class InvalidCodeException(message: String) : RuntimeException(message)
}

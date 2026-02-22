package zdrowy.senior.io.data.carelink

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.data.firestore.toSingle
import zdrowy.senior.io.domain.carelink.CareLink
import zdrowy.senior.io.domain.carelink.CareLinkCodeInfo
import zdrowy.senior.io.domain.carelink.CareLinkCode
import zdrowy.senior.io.domain.carelink.CareLinkCodeType
import zdrowy.senior.io.domain.carelink.CareLinkDraft
import zdrowy.senior.io.domain.carelink.CareLinkRepository
import zdrowy.senior.io.domain.carelink.CareLinkStatus
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
        val uid = requireUid()
        val expiresAtMs = System.currentTimeMillis() + ttlSeconds * 1000
        return createUniqueCode(
            remainingAttempts = 8,
            ownerUid = uid,
            expiresAtMs = expiresAtMs,
            type = type,
            draft = draft
        )
    }

    override fun getLinkCodeInfo(code: String): Single<CareLinkCodeInfo> {
        val doc = firestore.collection(FirestorePaths.ACCESS_CODES).document(code)
        return doc.get()
            .toSingle()
            .map { snapshot ->
                if (!snapshot.exists()) throw InvalidCodeException("Brak kodu")

                val expiresAtMs = (snapshot.get("expiresAtMs") as? Number)?.toLong()
                if (expiresAtMs != null && System.currentTimeMillis() > expiresAtMs) {
                    throw InvalidCodeException("Kod wygasl")
                }

                val typeRaw = snapshot.getString("type").orEmpty()
                val type = runCatching { CareLinkCodeType.valueOf(typeRaw) }
                    .getOrElse { throw InvalidCodeException("Nieprawidlowy typ kodu") }

                val draftPhone = snapshot.getString("draftPhoneNumber").orEmpty()

                CareLinkCodeInfo(
                    code = code,
                    type = type,
                    expiresAtMs = expiresAtMs,
                    draftPhoneNumber = draftPhone
                )
            }
    }

    override fun consumeLinkCode(code: String): Single<CareLink> {
        val uid = requireUid()
        val doc = firestore.collection(FirestorePaths.ACCESS_CODES).document(code)

        return firestore.runTransaction { tx ->
            val snapshot = tx.get(doc)
            if (!snapshot.exists()) throw InvalidCodeException("Brak kodu")

            val expiresAtMs = (snapshot.get("expiresAtMs") as? Number)?.toLong()
            if (expiresAtMs != null && System.currentTimeMillis() > expiresAtMs) {
                throw InvalidCodeException("Kod wygasl")
            }

            val typeRaw = snapshot.getString("type").orEmpty()
            val type = runCatching { CareLinkCodeType.valueOf(typeRaw) }
                .getOrElse { throw InvalidCodeException("Nieprawidlowy typ kodu") }

            val draft = CareLinkDraft(
                firstName = snapshot.getString("draftFirstName").orEmpty(),
                lastName = snapshot.getString("draftLastName").orEmpty(),
                pesel = snapshot.getString("draftPesel").orEmpty(),
                phoneNumber = snapshot.getString("draftPhoneNumber").orEmpty(),
                address = snapshot.getString("draftAddress").orEmpty()
            )

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
            CareLink(patientUid = patientUid, caregiverUid = caregiverUid, status = CareLinkStatus.ACTIVE)
        }.toSingle()
    }

    private fun createUniqueCode(
        remainingAttempts: Int,
        ownerUid: String,
        expiresAtMs: Long,
        type: CareLinkCodeType,
        draft: CareLinkDraft?
    ): Single<CareLinkCode> {
        if (remainingAttempts <= 0) {
            return Single.error(IllegalStateException("Unable to generate unique access code"))
        }

        val code = random.nextInt(100000, 1000000).toString()
        val accessCode = CareLinkCode(code = code, expiresAt = expiresAtMs, type = type)
        val doc = firestore.collection(FirestorePaths.ACCESS_CODES).document(code)

        return firestore.runTransaction { tx ->
            val snapshot = tx.get(doc)
            if (snapshot.exists()) throw CodeCollisionException()

            val payload = mutableMapOf<String, Any>(
                "type" to type.name,
                "ownerUid" to ownerUid,
                "expiresAtMs" to expiresAtMs,
                "createdAt" to FieldValue.serverTimestamp()
            )
            when (type) {
                CareLinkCodeType.PATIENT_TO_CAREGIVER -> payload["patientUid"] = ownerUid
                CareLinkCodeType.CAREGIVER_TO_PATIENT -> payload["caregiverUid"] = ownerUid
            }
            if (draft != null) {
                if (draft.firstName.isNotBlank()) payload["draftFirstName"] = draft.firstName
                if (draft.lastName.isNotBlank()) payload["draftLastName"] = draft.lastName
                if (draft.pesel.isNotBlank()) payload["draftPesel"] = draft.pesel
                if (draft.phoneNumber.isNotBlank()) payload["draftPhoneNumber"] = draft.phoneNumber
                if (draft.address.isNotBlank()) payload["draftAddress"] = draft.address
            }

            tx.set(doc, payload)
            true
        }
            .toSingle()
            .map { accessCode }
            .onErrorResumeNext { error ->
                if (error is CodeCollisionException) {
                    createUniqueCode(
                        remainingAttempts = remainingAttempts - 1,
                        ownerUid = ownerUid,
                        expiresAtMs = expiresAtMs,
                        type = type,
                        draft = draft
                    )
                } else {
                    Single.error(error)
                }
            }
    }

    private fun requireUid(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")
    }

    private fun requireRole(): UserRole {
        return roleContext.getRole() ?: throw IllegalStateException("User role not set")
    }

    private class CodeCollisionException : RuntimeException()

    private class InvalidCodeException(message: String) : RuntimeException(message)
}

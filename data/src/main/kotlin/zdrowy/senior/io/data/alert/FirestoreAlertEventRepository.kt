package zdrowy.senior.io.data.alert

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.data.firestore.PatientUidProvider
import zdrowy.senior.io.data.firestore.toSingle
import zdrowy.senior.io.domain.alert.AlertChannel
import zdrowy.senior.io.domain.alert.AlertEvent
import zdrowy.senior.io.domain.alert.AlertEventDraft
import zdrowy.senior.io.domain.alert.AlertEventRepository
import zdrowy.senior.io.domain.alert.AlertSeverity
import zdrowy.senior.io.domain.alert.BloodPressureEventData
import zdrowy.senior.io.domain.measurement.BloodPressureClassification
import zdrowy.senior.io.domain.measurement.BloodPressureSeverity
import zdrowy.senior.io.domain.measurement.BloodPressureStandard
import zdrowy.senior.io.domain.measurement.MeasurementType

class FirestoreAlertEventRepository(
    private val uidProvider: PatientUidProvider
) : AlertEventRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun addEvent(draft: AlertEventDraft): Single<String> {
        val uid = requireUid()
        val col = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.ALERT_EVENTS)

        val bp = draft.bloodPressure
        val payload = mutableMapOf<String, Any?>(
            "type" to draft.type.name,
            "severity" to draft.severity.name,
            "measurementId" to draft.measurementId,
            "reasons" to draft.reasons,
            "channelsPlanned" to draft.channelsPlanned.map { it.name },
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (bp != null) {
            payload["classification"] = mapOf(
                "standard" to bp.classification.standard.name,
                "severity" to bp.classification.severity.name,
                "isolatedSystolic" to bp.classification.isolatedSystolic,
                "systolic" to bp.systolic,
                "diastolic" to bp.diastolic
            )
        }

        return col.add(payload)
            .toSingle()
            .map { ref -> ref.id }
    }

    override fun getLastEvent(type: MeasurementType, severity: AlertSeverity): Maybe<AlertEvent> {
        val uid = try {
            requireUid()
        } catch (error: Throwable) {
            return Maybe.error(error)
        }
        val query = firestore.collection(FirestorePaths.USERS)
            .document(uid)
            .collection(FirestorePaths.ALERT_EVENTS)
            .whereEqualTo("type", type.name)
            .whereEqualTo("severity", severity.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)

        return query.get()
            .toSingle()
            .map { snapshot -> snapshot.documents.firstOrNull() }
            .flatMapMaybe { doc ->
                val event = doc?.toAlertEventOrNull()
                if (event != null) Maybe.just(event) else Maybe.empty()
            }
    }

    override fun observeRecentEvents(
        type: MeasurementType,
        limit: Int,
        severity: AlertSeverity?
    ): Observable<List<AlertEvent>> {
        return Observable.create { emitter ->
            val uid = try {
                requireUid()
            } catch (error: Throwable) {
                emitter.onError(error)
                return@create
            }
            var query: com.google.firebase.firestore.Query = firestore.collection(FirestorePaths.USERS)
                .document(uid)
                .collection(FirestorePaths.ALERT_EVENTS)
                .whereEqualTo("type", type.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            if (severity != null) {
                query = query.whereEqualTo("severity", severity.name)
            }
            query = query.limit(limit.toLong())

            val registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!emitter.isDisposed) emitter.onError(error)
                    return@addSnapshotListener
                }
                val docs = snapshot?.documents.orEmpty()
                val events = docs.mapNotNull { it.toAlertEventOrNull() }
                if (!emitter.isDisposed) emitter.onNext(events)
            }
            emitter.setCancellable { registration.remove() }
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toAlertEventOrNull(): AlertEvent? {
        val typeName = getString("type") ?: return null
        val severityName = getString("severity") ?: return null
        val measurementId = getString("measurementId") ?: return null
        val type = runCatching { MeasurementType.valueOf(typeName) }.getOrNull() ?: return null
        val severity = runCatching { AlertSeverity.valueOf(severityName) }.getOrNull() ?: return null
        val createdAt = getTimestamp("createdAt")?.toDate()?.time
        val reasons = (get("reasons") as? List<*>)?.mapNotNull { it as? String }.orEmpty()

        val classification = get("classification") as? Map<*, *>
        val bloodPressure = classification?.let { raw ->
            val standard = (raw["standard"] as? String)
                ?.let { runCatching { BloodPressureStandard.valueOf(it) }.getOrNull() }
                ?: BloodPressureStandard.ESC_ESH_OFFICE
            val severityBp = (raw["severity"] as? String)
                ?.let { runCatching { BloodPressureSeverity.valueOf(it) }.getOrNull() }
                ?: BloodPressureSeverity.HTN1
            val isolated = raw["isolatedSystolic"] as? Boolean ?: false
            val systolic = (raw["systolic"] as? Number)?.toInt() ?: return@let null
            val diastolic = (raw["diastolic"] as? Number)?.toInt() ?: return@let null
            BloodPressureEventData(
                systolic = systolic,
                diastolic = diastolic,
                classification = BloodPressureClassification(
                    standard = standard,
                    severity = severityBp,
                    isolatedSystolic = isolated
                )
            )
        }

        return AlertEvent(
            id = id,
            type = type,
            severity = severity,
            measurementId = measurementId,
            createdAt = createdAt,
            reasons = reasons,
            bloodPressure = bloodPressure
        )
    }

    private fun requireUid(): String = uidProvider.requirePatientUid()
}

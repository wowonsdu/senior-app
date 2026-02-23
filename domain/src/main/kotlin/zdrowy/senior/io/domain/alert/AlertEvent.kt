package zdrowy.senior.io.domain.alert

import zdrowy.senior.io.domain.measurement.BloodPressureClassification
import zdrowy.senior.io.domain.measurement.MeasurementType

data class AlertEvent(
    val id: String,
    val type: MeasurementType,
    val severity: AlertSeverity,
    val measurementId: String,
    val createdAt: Long?,
    val reasons: List<String>,
    val bloodPressure: BloodPressureEventData?
)

data class BloodPressureEventData(
    val systolic: Int,
    val diastolic: Int,
    val classification: BloodPressureClassification
)

data class AlertEventDraft(
    val type: MeasurementType,
    val severity: AlertSeverity,
    val measurementId: String,
    val reasons: List<String>,
    val bloodPressure: BloodPressureEventData?,
    val channelsPlanned: Set<AlertChannel>
)


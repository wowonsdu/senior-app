package zdrowy.senior.io.ui.caregiver.model

import zdrowy.senior.io.domain.measurement.MeasurementType

data class CaregiverMeasurementNotificationUi(
    val id: String,
    val patientUid: String,
    val patientName: String,
    val type: MeasurementType,
    val typeLabel: String,
    val valueLabel: String,
    val timeLabel: String,
    val iconRes: Int,
    val iconTintRes: Int,
    val chipColorRes: Int,
    val timestamp: Long,
    val isRead: Boolean
)

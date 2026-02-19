package zdrowy.senior.io.domain.alert

import zdrowy.senior.io.domain.measurement.MeasurementType

data class AlertSetting(
    val type: MeasurementType,
    val enabled: Boolean,
    val min: Double?,
    val max: Double?,
    val spikePercent: Int,
    val windowCount: Int,
    val channels: Set<AlertChannel>,
    val caregiverIds: List<String>
)

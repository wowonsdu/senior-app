package zdrowy.senior.io.domain.alert

import zdrowy.senior.io.domain.measurement.MeasurementType

data class AlertSetting(
    val type: MeasurementType,
    val enabled: Boolean,
    val lowThreshold: String,
    val highThreshold: String
)

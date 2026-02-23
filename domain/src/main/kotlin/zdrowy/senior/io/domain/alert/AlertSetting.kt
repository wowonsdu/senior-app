package zdrowy.senior.io.domain.alert

import zdrowy.senior.io.domain.measurement.BloodPressureSeverity
import zdrowy.senior.io.domain.measurement.BloodPressureStandard
import zdrowy.senior.io.domain.measurement.MeasurementType

data class AlertSetting(
    val type: MeasurementType,
    val enabled: Boolean,
    val min: Double?,
    val max: Double?,
    val spikePercent: Int,
    val windowCount: Int,
    val channels: Set<AlertChannel>,
    val caregiverIds: List<String>,
    val systolicMin: Double? = null,
    val systolicMax: Double? = null,
    val diastolicMin: Double? = null,
    val diastolicMax: Double? = null,
    val categoryEnabled: Boolean = false,
    val categoryThreshold: BloodPressureSeverity = BloodPressureSeverity.HTN1,
    val categoryCooldownMinutes: Int = 60,
    val bpStandard: BloodPressureStandard = BloodPressureStandard.ESC_ESH_OFFICE
)

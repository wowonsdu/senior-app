package zdrowy.senior.io.domain.measurement

data class BloodPressureClassification(
    val standard: BloodPressureStandard,
    val severity: BloodPressureSeverity,
    val isolatedSystolic: Boolean
)


package zdrowy.senior.io.domain.measurement

data class ParsedVoiceMeasurement(
    val type: MeasurementType,
    val value: Double? = null,
    val systolic: Int? = null,
    val diastolic: Int? = null
)

package zdrowy.senior.io.domain.measurement

data class Measurement(
    val id: String,
    val type: MeasurementType,
    val value: Double?,
    val systolic: Int?,
    val diastolic: Int?,
    val timestamp: Long,
    val source: MeasurementSource
)

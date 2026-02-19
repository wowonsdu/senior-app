package zdrowy.senior.io.domain.measurement

data class Measurement(
    val id: String,
    val patientId: String,
    val type: MeasurementType,
    val valueRaw: String,
    val createdAt: Long
)

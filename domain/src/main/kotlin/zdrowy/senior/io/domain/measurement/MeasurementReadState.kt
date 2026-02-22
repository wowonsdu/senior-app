package zdrowy.senior.io.domain.measurement

data class MeasurementReadState(
    val patientUid: String,
    val lastReadAtMs: Long
)

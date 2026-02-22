package zdrowy.senior.io.domain.measurement

data class MeasurementReadState(
    val patientUid: String,
    val readMeasurementIds: Set<String>
)

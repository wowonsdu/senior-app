package zdrowy.senior.io.ui.patient

data class PatientMeasurementItemUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconRes: Int,
    val iconTintRes: Int,
    val type: zdrowy.senior.io.domain.measurement.MeasurementType,
    val timestamp: Long,
    val value: Double?,
    val systolic: Int?,
    val diastolic: Int?
)

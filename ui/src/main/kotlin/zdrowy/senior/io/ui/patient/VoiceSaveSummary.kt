package zdrowy.senior.io.ui.patient

import zdrowy.senior.io.domain.measurement.MeasurementType

data class VoiceSaveSummary(
    val savedTypes: List<MeasurementType>,
    val skippedTypes: List<MeasurementType>
)

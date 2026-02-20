package zdrowy.senior.io.domain.history

import zdrowy.senior.io.domain.measurement.MeasurementType

data class ChartSeries(
    val type: MeasurementType,
    val points: List<ChartPoint>,
    val secondaryPoints: List<ChartPoint> = emptyList()
)

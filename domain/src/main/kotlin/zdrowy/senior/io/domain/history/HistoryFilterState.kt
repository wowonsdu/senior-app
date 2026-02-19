package zdrowy.senior.io.domain.history

import zdrowy.senior.io.domain.measurement.MeasurementType

data class HistoryFilterState(
    val availableTypes: List<MeasurementType>,
    val availableRanges: List<HistoryRange>,
    val selectedTypes: List<MeasurementType>,
    val selectedRange: HistoryRange
)

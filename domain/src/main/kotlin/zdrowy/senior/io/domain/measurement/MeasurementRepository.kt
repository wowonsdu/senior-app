package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.domain.history.ChartSeries
import zdrowy.senior.io.domain.history.DateRange
import zdrowy.senior.io.domain.history.HistoryFilterState

interface MeasurementRepository {
    fun addMeasurement(
        type: MeasurementType,
        value: Double,
        timestamp: Long,
        source: MeasurementSource
    ): Single<String>

    fun addBloodPressureMeasurement(
        systolic: Int,
        diastolic: Int,
        timestamp: Long,
        source: MeasurementSource
    ): Single<String>

    fun getRecentMeasurements(
        limit: Int,
        types: List<MeasurementType>? = null
    ): Single<List<Measurement>>

    fun getMeasurementHistory(
        types: List<MeasurementType>?,
        dateRange: DateRange?
    ): Single<List<Measurement>>

    fun getMeasurementChartData(
        types: List<MeasurementType>?,
        dateRange: DateRange?
    ): Single<List<ChartSeries>>

    fun getHistoryFilters(): Single<HistoryFilterState>
}

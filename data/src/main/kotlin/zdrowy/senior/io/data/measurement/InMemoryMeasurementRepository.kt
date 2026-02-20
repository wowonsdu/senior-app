package zdrowy.senior.io.data.measurement

import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.domain.history.ChartPoint
import zdrowy.senior.io.domain.history.ChartSeries
import zdrowy.senior.io.domain.history.DateRange
import zdrowy.senior.io.domain.history.HistoryFilterState
import zdrowy.senior.io.domain.history.HistoryRange
import zdrowy.senior.io.domain.measurement.Measurement
import zdrowy.senior.io.domain.measurement.MeasurementRepository
import zdrowy.senior.io.domain.measurement.MeasurementSource
import zdrowy.senior.io.domain.measurement.MeasurementType
import java.util.UUID

class InMemoryMeasurementRepository : MeasurementRepository {
    private val measurements = mutableListOf<Measurement>()

    init {
        seedData()
    }

    override fun addMeasurement(
        type: MeasurementType,
        value: Double,
        timestamp: Long,
        source: MeasurementSource
    ): Single<String> {
        val id = UUID.randomUUID().toString()
        measurements.add(
            Measurement(
                id = id,
                type = type,
                value = value,
                systolic = null,
                diastolic = null,
                timestamp = timestamp,
                source = source
            )
        )
        return Single.just(id)
    }

    override fun addBloodPressureMeasurement(
        systolic: Int,
        diastolic: Int,
        timestamp: Long,
        source: MeasurementSource
    ): Single<String> {
        val id = UUID.randomUUID().toString()
        measurements.add(
            Measurement(
                id = id,
                type = MeasurementType.PRESSURE,
                value = null,
                systolic = systolic,
                diastolic = diastolic,
                timestamp = timestamp,
                source = source
            )
        )
        return Single.just(id)
    }

    override fun getRecentMeasurements(
        limit: Int,
        types: List<MeasurementType>?
    ): Single<List<Measurement>> {
        val filtered = applyFilters(types, null)
        return Single.just(filtered.sortedByDescending { it.timestamp }.take(limit))
    }

    override fun getMeasurementHistory(
        types: List<MeasurementType>?,
        dateRange: DateRange?
    ): Single<List<Measurement>> = Single.just(applyFilters(types, dateRange))

    override fun getMeasurementChartData(
        types: List<MeasurementType>?,
        dateRange: DateRange?
    ): Single<List<ChartSeries>> {
        val filtered = applyFilters(types, dateRange)
        val grouped = filtered.groupBy { it.type }
        val series = grouped.map { (type, list) ->
            val points = list.sortedBy { it.timestamp }.mapNotNull { measurement ->
                val value = measurement.value
                    ?: measurement.systolic?.toDouble()
                    ?: measurement.diastolic?.toDouble()
                value?.let { ChartPoint(measurement.timestamp, it) }
            }
            ChartSeries(type, points)
        }
        return Single.just(series)
    }

    override fun getHistoryFilters(): Single<HistoryFilterState> {
        val types = MeasurementType.values().toList()
        val ranges = HistoryRange.values().toList()
        return Single.just(
            HistoryFilterState(
                availableTypes = types,
                availableRanges = ranges,
                selectedTypes = types,
                selectedRange = HistoryRange.LAST_30_DAYS
            )
        )
    }

    override fun updateMeasurement(
        id: String,
        type: MeasurementType,
        value: Double,
        timestamp: Long
    ): Single<Unit> {
        val index = measurements.indexOfFirst { it.id == id }
        if (index == -1) return Single.error(IllegalArgumentException("Measurement not found"))
        val current = measurements[index]
        measurements[index] = current.copy(
            type = type,
            value = value,
            systolic = null,
            diastolic = null,
            timestamp = timestamp
        )
        return Single.just(Unit)
    }

    override fun updateBloodPressureMeasurement(
        id: String,
        systolic: Int,
        diastolic: Int,
        timestamp: Long
    ): Single<Unit> {
        val index = measurements.indexOfFirst { it.id == id }
        if (index == -1) return Single.error(IllegalArgumentException("Measurement not found"))
        val current = measurements[index]
        measurements[index] = current.copy(
            type = MeasurementType.PRESSURE,
            value = null,
            systolic = systolic,
            diastolic = diastolic,
            timestamp = timestamp
        )
        return Single.just(Unit)
    }

    override fun deleteMeasurement(id: String): Single<Unit> {
        measurements.removeAll { it.id == id }
        return Single.just(Unit)
    }

    private fun applyFilters(
        types: List<MeasurementType>?,
        dateRange: DateRange?
    ): List<Measurement> {
        return measurements.filter { measurement ->
            val typeMatch = types?.contains(measurement.type) ?: true
            val fromMatch = dateRange?.from?.let { measurement.timestamp >= it } ?: true
            val toMatch = dateRange?.to?.let { measurement.timestamp <= it } ?: true
            typeMatch && fromMatch && toMatch
        }
    }

    private fun seedData() {
        val now = System.currentTimeMillis()
        measurements += Measurement(
            id = UUID.randomUUID().toString(),
            type = MeasurementType.SUGAR,
            value = 102.0,
            systolic = null,
            diastolic = null,
            timestamp = now - 60 * 60 * 1000,
            source = MeasurementSource.MANUAL
        )
        measurements += Measurement(
            id = UUID.randomUUID().toString(),
            type = MeasurementType.INSULIN,
            value = 8.0,
            systolic = null,
            diastolic = null,
            timestamp = now - 2 * 60 * 60 * 1000,
            source = MeasurementSource.MANUAL
        )
        measurements += Measurement(
            id = UUID.randomUUID().toString(),
            type = MeasurementType.PRESSURE,
            value = null,
            systolic = 128,
            diastolic = 82,
            timestamp = now - 3 * 60 * 60 * 1000,
            source = MeasurementSource.MANUAL
        )
        measurements += Measurement(
            id = UUID.randomUUID().toString(),
            type = MeasurementType.PULSE,
            value = 74.0,
            systolic = null,
            diastolic = null,
            timestamp = now - 4 * 60 * 60 * 1000,
            source = MeasurementSource.VOICE
        )
    }
}

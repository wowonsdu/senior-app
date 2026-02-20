package zdrowy.senior.io.data.measurement

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
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
    private val measurementsSubject = BehaviorSubject.create<List<Measurement>>()

    init {
        seedData()
        measurementsSubject.onNext(measurements.toList())
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
        measurementsSubject.onNext(measurements.toList())
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
        measurementsSubject.onNext(measurements.toList())
        return Single.just(id)
    }

    override fun getRecentMeasurements(
        limit: Int,
        types: List<MeasurementType>?
    ): Single<List<Measurement>> {
        val filtered = applyFilters(measurements, types, null)
        return Single.just(filtered.sortedByDescending { it.timestamp }.take(limit))
    }

    override fun observeRecentMeasurements(
        limit: Int,
        types: List<MeasurementType>?
    ): Observable<List<Measurement>> {
        return measurementsSubject.map { list ->
            applyFilters(list, types, null).sortedByDescending { it.timestamp }.take(limit)
        }
    }

    override fun getMeasurementHistory(
        types: List<MeasurementType>?,
        dateRange: DateRange?
    ): Single<List<Measurement>> = Single.just(applyFilters(measurements, types, dateRange))

    override fun observeMeasurementHistory(
        types: List<MeasurementType>?,
        dateRange: DateRange?
    ): Observable<List<Measurement>> = measurementsSubject.map { list ->
        applyFilters(list, types, dateRange)
    }

    override fun getMeasurementChartData(
        types: List<MeasurementType>?,
        dateRange: DateRange?
    ): Single<List<ChartSeries>> {
        val filtered = applyFilters(measurements, types, dateRange)
        val grouped = filtered.groupBy { it.type }
        val series = grouped.map { (type, list) ->
            val sorted = list.sortedBy { it.timestamp }
            val points = sorted.mapNotNull { measurement ->
                val value = measurement.value
                    ?: measurement.systolic?.toDouble()
                    ?: measurement.diastolic?.toDouble()
                value?.let { ChartPoint(measurement.timestamp, it) }
            }
            val secondaryPoints = if (type == MeasurementType.PRESSURE) {
                sorted.mapNotNull { measurement ->
                    measurement.diastolic?.toDouble()
                        ?.let { ChartPoint(measurement.timestamp, it) }
                }
            } else {
                emptyList()
            }
            val primaryPoints = if (type == MeasurementType.PRESSURE) {
                sorted.mapNotNull { measurement ->
                    measurement.systolic?.toDouble()
                        ?.let { ChartPoint(measurement.timestamp, it) }
                }
            } else {
                points
            }
            ChartSeries(type, primaryPoints, secondaryPoints)
        }
        return Single.just(series)
    }

    override fun observeMeasurementChartData(
        types: List<MeasurementType>?,
        dateRange: DateRange?
    ): Observable<List<ChartSeries>> {
        return observeMeasurementHistory(types, dateRange)
            .map { filtered ->
                val grouped = filtered.groupBy { it.type }
                grouped.map { (type, list) ->
                    val sorted = list.sortedBy { it.timestamp }
                    val points = sorted.mapNotNull { measurement ->
                        val value = measurement.value
                            ?: measurement.systolic?.toDouble()
                            ?: measurement.diastolic?.toDouble()
                        value?.let { ChartPoint(measurement.timestamp, it) }
                    }
                    val secondaryPoints = if (type == MeasurementType.PRESSURE) {
                        sorted.mapNotNull { measurement ->
                            measurement.diastolic?.toDouble()
                                ?.let { ChartPoint(measurement.timestamp, it) }
                        }
                    } else {
                        emptyList()
                    }
                    val primaryPoints = if (type == MeasurementType.PRESSURE) {
                        sorted.mapNotNull { measurement ->
                            measurement.systolic?.toDouble()
                                ?.let { ChartPoint(measurement.timestamp, it) }
                        }
                    } else {
                        points
                    }
                    ChartSeries(type, primaryPoints, secondaryPoints)
                }
            }
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
        measurementsSubject.onNext(measurements.toList())
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
        measurementsSubject.onNext(measurements.toList())
        return Single.just(Unit)
    }

    override fun deleteMeasurement(id: String): Single<Unit> {
        measurements.removeAll { it.id == id }
        measurementsSubject.onNext(measurements.toList())
        return Single.just(Unit)
    }

    private fun applyFilters(
        source: List<Measurement>,
        types: List<MeasurementType>?,
        dateRange: DateRange?
    ): List<Measurement> {
        return source.filter { measurement ->
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

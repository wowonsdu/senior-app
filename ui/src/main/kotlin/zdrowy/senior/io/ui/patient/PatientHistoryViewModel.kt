package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.history.ChartSeries
import zdrowy.senior.io.domain.history.GetHistoryFiltersUseCase
import zdrowy.senior.io.domain.history.HistoryFilterState
import zdrowy.senior.io.domain.measurement.Measurement
import zdrowy.senior.io.domain.measurement.ObserveMeasurementChartDataUseCase
import zdrowy.senior.io.domain.measurement.ObserveMeasurementHistoryUseCase
import zdrowy.senior.io.domain.measurement.MeasurementType
import zdrowy.senior.io.ui.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PatientHistoryViewModel(
    private val observeMeasurementHistory: ObserveMeasurementHistoryUseCase,
    private val observeMeasurementChartData: ObserveMeasurementChartDataUseCase,
    private val getHistoryFilters: GetHistoryFiltersUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val historyDisposable = SerialDisposable()
    private val chartDisposable = SerialDisposable()
    private var selectedTypes: List<MeasurementType> = emptyList()
    private var latestMeasurements: List<Measurement> = emptyList()
    private var latestSeries: List<ChartSeries> = emptyList()

    private val _measurements = MutableLiveData<List<PatientMeasurementItemUi>>()
    val measurements: LiveData<List<PatientMeasurementItemUi>> = _measurements

    private val _chartSeries = MutableLiveData<List<ChartSeries>>()
    val chartSeries: LiveData<List<ChartSeries>> = _chartSeries

    private val _filters = MutableLiveData<HistoryFilterState>()
    val filters: LiveData<HistoryFilterState> = _filters

    init {
        disposables.addAll(historyDisposable, chartDisposable)
    }

    fun load() {
        observeHistoryAll()
        observeChartAll()
        disposables.add(
            getHistoryFilters()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ state ->
                    val selected = state.selectedTypes.ifEmpty { MeasurementType.values().toList() }
                    selectedTypes = selected
                    _filters.value = state.copy(selectedTypes = selected)
                    publishFiltered()
                }, {
                })
        )
    }

    fun setSelectedTypes(types: List<MeasurementType>) {
        if (types.isEmpty()) return
        val current = _filters.value
        if (current != null) {
            _filters.value = current.copy(selectedTypes = types)
        }
        selectedTypes = types
        publishFiltered()
    }

    private fun observeHistoryAll() {
        historyDisposable.set(
            observeMeasurementHistory(null, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ items ->
                    latestMeasurements = items
                    publishFiltered()
                }, {
                })
        )
    }

    private fun observeChartAll() {
        chartDisposable.set(
            observeMeasurementChartData(null, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ series ->
                    latestSeries = series
                    publishFiltered()
                }, {
                })
        )
    }

    private fun publishFiltered() {
        val activeTypes = selectedTypes.ifEmpty { MeasurementType.values().toList() }
        val formatter = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
        _measurements.value = latestMeasurements
            .filter { activeTypes.contains(it.type) }
            .map { measurement ->
                val title = when (measurement.type) {
                    MeasurementType.PRESSURE -> {
                        val systolic = measurement.systolic ?: 0
                        val diastolic = measurement.diastolic ?: 0
                        "Cisnienie: ${systolic}/${diastolic} mmHg"
                    }
                    MeasurementType.SUGAR -> "Cukier: ${measurement.value ?: 0.0} mg/dl"
                    MeasurementType.INSULIN -> "Insulina: ${measurement.value ?: 0.0} j."
                    MeasurementType.PULSE -> "Tetno: ${measurement.value ?: 0.0} bpm"
                }
                PatientMeasurementItemUi(
                    id = measurement.id,
                    title = title,
                    subtitle = formatter.format(Date(measurement.timestamp)),
                    iconRes = iconFor(measurement.type),
                    iconTintRes = tintFor(measurement.type),
                    type = measurement.type,
                    timestamp = measurement.timestamp,
                    value = measurement.value,
                    systolic = measurement.systolic,
                    diastolic = measurement.diastolic
                )
            }
        _chartSeries.value = latestSeries.filter { activeTypes.contains(it.type) }
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    private fun iconFor(type: MeasurementType): Int {
        return when (type) {
            MeasurementType.SUGAR -> R.drawable.ic_alert_drop
            MeasurementType.INSULIN -> R.drawable.ic_alert_medical
            MeasurementType.PRESSURE -> R.drawable.ic_alert_heart
            MeasurementType.PULSE -> R.drawable.ic_alert_pulse
        }
    }

    private fun tintFor(type: MeasurementType): Int {
        return when (type) {
            MeasurementType.SUGAR -> R.color.senior_info
            MeasurementType.INSULIN -> R.color.senior_secondary
            MeasurementType.PRESSURE -> R.color.senior_danger
            MeasurementType.PULSE -> R.color.senior_purple
        }
    }
}

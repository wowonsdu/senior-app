package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.history.ChartSeries
import zdrowy.senior.io.domain.history.GetHistoryFiltersUseCase
import zdrowy.senior.io.domain.history.GetMeasurementChartDataUseCase
import zdrowy.senior.io.domain.history.GetMeasurementHistoryUseCase
import zdrowy.senior.io.domain.history.HistoryFilterState
import zdrowy.senior.io.domain.measurement.Measurement
import zdrowy.senior.io.domain.measurement.MeasurementType

class PatientHistoryViewModel(
    private val getMeasurementHistory: GetMeasurementHistoryUseCase,
    private val getMeasurementChartData: GetMeasurementChartDataUseCase,
    private val getHistoryFilters: GetHistoryFiltersUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()

    private val _measurements = MutableLiveData<List<Measurement>>()
    val measurements: LiveData<List<Measurement>> = _measurements

    private val _chartSeries = MutableLiveData<List<ChartSeries>>()
    val chartSeries: LiveData<List<ChartSeries>> = _chartSeries

    private val _filters = MutableLiveData<HistoryFilterState>()
    val filters: LiveData<HistoryFilterState> = _filters

    fun load() {
        disposables.add(
            getHistoryFilters()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ state ->
                    val selected = state.selectedTypes.ifEmpty { MeasurementType.values().toList() }
                    _filters.value = state.copy(selectedTypes = selected)
                    loadHistory(selected)
                    loadChart(selected)
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
        loadHistory(types)
        loadChart(types)
    }

    private fun loadHistory(types: List<MeasurementType>) {
        disposables.add(
            getMeasurementHistory(types, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ items ->
                    _measurements.value = items
                }, {
                })
        )
    }

    private fun loadChart(types: List<MeasurementType>) {
        disposables.add(
            getMeasurementChartData(types, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ series ->
                    _chartSeries.value = series
                }, {
                })
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    class Factory(
        private val getMeasurementHistory: GetMeasurementHistoryUseCase,
        private val getMeasurementChartData: GetMeasurementChartDataUseCase,
        private val getHistoryFilters: GetHistoryFiltersUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PatientHistoryViewModel::class.java)) {
                return PatientHistoryViewModel(
                    getMeasurementHistory,
                    getMeasurementChartData,
                    getHistoryFilters
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

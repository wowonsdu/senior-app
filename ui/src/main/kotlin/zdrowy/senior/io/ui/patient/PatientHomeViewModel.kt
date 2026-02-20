package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.measurement.GetRecentMeasurementsUseCase
import zdrowy.senior.io.domain.measurement.Measurement

class PatientHomeViewModel(
    private val getRecentMeasurements: GetRecentMeasurementsUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _recentMeasurements = MutableLiveData<List<Measurement>>()
    val recentMeasurements: LiveData<List<Measurement>> = _recentMeasurements

    fun loadRecent(limit: Int = 4) {
        disposables.add(
            getRecentMeasurements(limit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ measurements ->
                    _recentMeasurements.value = measurements
                }, {
                })
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

}

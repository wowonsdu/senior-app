package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.alert.AlertConfig
import zdrowy.senior.io.domain.alert.GetAlertConfigUseCase

class PatientAlertsViewModel(
    private val getAlertConfig: GetAlertConfigUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _alertConfig = MutableLiveData<AlertConfig>()
    val alertConfig: LiveData<AlertConfig> = _alertConfig

    fun load() {
        disposables.add(
            getAlertConfig()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ config ->
                    _alertConfig.value = config
                }, {
                })
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    class Factory(
        private val getAlertConfig: GetAlertConfigUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PatientAlertsViewModel::class.java)) {
                return PatientAlertsViewModel(getAlertConfig) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

package zdrowy.senior.io.ui.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import zdrowy.senior.io.domain.alert.AlertSetting
import zdrowy.senior.io.domain.alert.ObserveAlertSettingsUseCase
import zdrowy.senior.io.domain.alert.UpdateAlertSettingsUseCase
import zdrowy.senior.io.domain.measurement.MeasurementType
import zdrowy.senior.io.domain.session.GetActivePatientUseCase
import zdrowy.senior.io.domain.session.GetSessionUseCase
import zdrowy.senior.io.domain.session.UserRole

class AlertSettingsViewModel : ViewModel(), KoinComponent {
    private val getSessionUseCase: GetSessionUseCase by inject()
    private val getActivePatientUseCase: GetActivePatientUseCase by inject()
    private val observeAlertSettingsUseCase: ObserveAlertSettingsUseCase by inject()
    private val updateAlertSettingsUseCase: UpdateAlertSettingsUseCase by inject()

    private val disposables = CompositeDisposable()
    private val _settings = MutableLiveData<List<AlertSetting>>()
    val settings: LiveData<List<AlertSetting>> = _settings

    private var patientId: String? = null

    fun start() {
        val session = getSessionUseCase.execute() ?: return
        patientId = when (session.role) {
            UserRole.PATIENT -> session.uid
            UserRole.CAREGIVER -> getActivePatientUseCase.execute()
        }
        val pid = patientId ?: return
        val disposable = observeAlertSettingsUseCase.execute(pid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ list ->
                _settings.value = list
            }, {
                _settings.value = emptyList()
            })
        disposables.add(disposable)
    }

    fun saveSettings(updated: List<AlertSetting>) {
        val pid = patientId ?: return
        updateAlertSettingsUseCase.execute(pid, updated)
    }

    fun defaultSettings(): List<AlertSetting> {
        return listOf(
            AlertSetting(MeasurementType.GLUCOSE, true, "70", "180"),
            AlertSetting(MeasurementType.INSULIN, true, "5", "15"),
            AlertSetting(MeasurementType.PRESSURE, true, "90", "140"),
            AlertSetting(MeasurementType.PULSE, true, "60", "100")
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}


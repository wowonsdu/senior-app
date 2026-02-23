package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.alert.ObserveAlertConfigUseCase
import zdrowy.senior.io.domain.alert.SetAlertEnabledUseCase
import zdrowy.senior.io.domain.alert.UpdateBloodPressureCategoryRulesUseCase
import zdrowy.senior.io.domain.alert.UpdateBloodPressureCriticalThresholdsUseCase
import zdrowy.senior.io.domain.measurement.BloodPressureSeverity
import zdrowy.senior.io.domain.measurement.BloodPressureStandard
import zdrowy.senior.io.domain.measurement.MeasurementType

class PatientAlertsViewModel(
    private val observeAlertConfig: ObserveAlertConfigUseCase,
    private val setAlertEnabled: SetAlertEnabledUseCase,
    private val updateBloodPressureCriticalThresholds: UpdateBloodPressureCriticalThresholdsUseCase,
    private val updateBloodPressureCategoryRules: UpdateBloodPressureCategoryRulesUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val configDisposable = SerialDisposable()

    private val _pressureConfig = MutableLiveData<PressureAlertConfigUi>()
    val pressureConfig: LiveData<PressureAlertConfigUi> = _pressureConfig

    private val _saveResult = MutableLiveData<Boolean?>()
    val saveResult: LiveData<Boolean?> = _saveResult

    init {
        disposables.add(configDisposable)
    }

    fun start() {
        configDisposable.set(
            observeAlertConfig()
                .subscribeOn(Schedulers.io())
                .map { config ->
                    val setting = config.settings.firstOrNull { it.type == MeasurementType.PRESSURE }
                    if (setting == null) {
                        PressureAlertConfigUi.fallback()
                    } else {
                        PressureAlertConfigUi(
                            enabled = setting.enabled,
                            systolicMin = setting.systolicMin ?: setting.min,
                            systolicMax = setting.systolicMax ?: setting.max,
                            diastolicMin = setting.diastolicMin,
                            diastolicMax = setting.diastolicMax,
                            categoryEnabled = setting.categoryEnabled,
                            categoryThreshold = setting.categoryThreshold,
                            categoryCooldownMinutes = setting.categoryCooldownMinutes,
                            standard = setting.bpStandard
                        )
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ ui ->
                    _pressureConfig.value = ui
                }, {
                })
        )
    }

    fun setPressureEnabled(enabled: Boolean) {
        disposables.add(
            setAlertEnabled(MeasurementType.PRESSURE, enabled)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }

    fun savePressureConfig(
        systolicMin: Double?,
        systolicMax: Double?,
        diastolicMin: Double?,
        diastolicMax: Double?,
        categoryEnabled: Boolean,
        categoryThreshold: BloodPressureSeverity,
        categoryCooldownMinutes: Int,
        standard: BloodPressureStandard = BloodPressureStandard.ESC_ESH_OFFICE
    ) {
        val save = Completable.concatArray(
            updateBloodPressureCriticalThresholds(
                systolicMin = systolicMin,
                systolicMax = systolicMax,
                diastolicMin = diastolicMin,
                diastolicMax = diastolicMax
            ),
            updateBloodPressureCategoryRules(
                enabled = categoryEnabled,
                threshold = categoryThreshold,
                cooldownMinutes = categoryCooldownMinutes,
                standard = standard
            )
        )
        disposables.add(
            save.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _saveResult.value = true
                }, {
                    _saveResult.value = false
                })
        )
    }

    fun onSaveResultHandled() {
        _saveResult.value = null
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}

data class PressureAlertConfigUi(
    val enabled: Boolean,
    val systolicMin: Double?,
    val systolicMax: Double?,
    val diastolicMin: Double?,
    val diastolicMax: Double?,
    val categoryEnabled: Boolean,
    val categoryThreshold: BloodPressureSeverity,
    val categoryCooldownMinutes: Int,
    val standard: BloodPressureStandard
) {
    companion object {
        fun fallback(): PressureAlertConfigUi = PressureAlertConfigUi(
            enabled = true,
            systolicMin = null,
            systolicMax = null,
            diastolicMin = null,
            diastolicMax = null,
            categoryEnabled = true,
            categoryThreshold = BloodPressureSeverity.HTN1,
            categoryCooldownMinutes = 60,
            standard = BloodPressureStandard.ESC_ESH_OFFICE
        )
    }
}

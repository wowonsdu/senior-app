package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.agent.Agent
import zdrowy.senior.io.domain.agent.AgentRole
import zdrowy.senior.io.domain.agent.ObserveAgentsUseCase
import zdrowy.senior.io.domain.alert.AlertChannel
import zdrowy.senior.io.domain.alert.AlertSetting
import zdrowy.senior.io.domain.alert.ObserveAlertConfigUseCase
import zdrowy.senior.io.domain.alert.SetAlertEnabledUseCase
import zdrowy.senior.io.domain.alert.UpdateAlertCaregiversUseCase
import zdrowy.senior.io.domain.alert.UpdateAlertChannelsUseCase
import zdrowy.senior.io.domain.alert.UpdateBloodPressureCriticalThresholdsUseCase
import zdrowy.senior.io.domain.measurement.MeasurementType

class PatientAlertsViewModel(
    private val observeAlertConfig: ObserveAlertConfigUseCase,
    private val setAlertEnabled: SetAlertEnabledUseCase,
    private val updateBloodPressureCriticalThresholds: UpdateBloodPressureCriticalThresholdsUseCase,
    private val updateAlertChannels: UpdateAlertChannelsUseCase,
    private val updateAlertCaregivers: UpdateAlertCaregiversUseCase,
    private val observeAgents: ObserveAgentsUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val configDisposable = SerialDisposable()
    private var latestSettings: Map<MeasurementType, AlertSetting> = emptyMap()
    private var latestCaregivers: List<Agent> = emptyList()

    private val _pressureConfig = MutableLiveData<PressureAlertConfigUi>()
    val pressureConfig: LiveData<PressureAlertConfigUi> = _pressureConfig

    private val _notifications = MutableLiveData<Map<MeasurementType, AlertNotificationsUi>>()
    val notifications: LiveData<Map<MeasurementType, AlertNotificationsUi>> = _notifications

    private val _saveResult = MutableLiveData<Boolean?>()
    val saveResult: LiveData<Boolean?> = _saveResult

    init {
        disposables.add(configDisposable)
    }

    fun start() {
        configDisposable.set(
            Observable.combineLatest(
                observeAlertConfig(),
                observeAgents().map { agents -> agents.filter { it.role == AgentRole.CAREGIVER } }
            ) { config, caregivers -> Pair(config, caregivers) }
                .subscribeOn(Schedulers.io())
                .map { (config, caregivers) ->
                    latestSettings = config.settings.associateBy { it.type }
                    latestCaregivers = caregivers

                    val notifications = MeasurementType.values().associateWith { type ->
                        val setting = latestSettings[type]
                        val enabled = setting?.enabled ?: true
                        val channels = setting?.channels.orEmpty()
                        val caregiverIds = setting?.caregiverIds.orEmpty()
                        AlertNotificationsUi(
                            enabled = enabled,
                            channels = channels,
                            caregivers = caregivers.map { agent ->
                                AlertNotificationsUi.CaregiverUi(
                                    id = agent.id,
                                    fullName = agent.fullName,
                                    phone = agent.phone,
                                    selected = caregiverIds.contains(agent.id)
                                )
                            }
                        )
                    }

                    val setting = config.settings.firstOrNull { it.type == MeasurementType.PRESSURE }
                    val pressureUi = if (setting == null) {
                        PressureAlertConfigUi.fallback()
                    } else {
                        PressureAlertConfigUi(
                            enabled = setting.enabled,
                            systolicMin = setting.systolicMin ?: setting.min,
                            systolicMax = setting.systolicMax ?: setting.max,
                            diastolicMin = setting.diastolicMin,
                            diastolicMax = setting.diastolicMax
                        )
                    }

                    Pair(pressureUi, notifications)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ (pressure, notifications) ->
                    _pressureConfig.value = pressure
                    _notifications.value = notifications
                }, {
                })
        )
    }

    fun setEnabled(type: MeasurementType, enabled: Boolean) {
        disposables.add(
            setAlertEnabled(type, enabled)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }

    fun toggleChannel(type: MeasurementType, channel: AlertChannel, enabled: Boolean) {
        val current = latestSettings[type]?.channels.orEmpty().toMutableSet()
        if (enabled) current.add(channel) else current.remove(channel)
        disposables.add(
            updateAlertChannels(type, current.toSet())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }

    fun enableAllChannels(type: MeasurementType) {
        val all = setOf(
            AlertChannel.SMS,
            AlertChannel.EMAIL,
            AlertChannel.APP
        )
        disposables.add(
            updateAlertChannels(type, all)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }

    fun disableAllChannels(type: MeasurementType) {
        disposables.add(
            updateAlertChannels(type, emptySet())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }

    fun toggleCaregiver(type: MeasurementType, caregiverId: String, enabled: Boolean) {
        val current = latestSettings[type]?.caregiverIds.orEmpty().toMutableSet()
        if (enabled) current.add(caregiverId) else current.remove(caregiverId)
        disposables.add(
            updateAlertCaregivers(type, current.toList())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }

    fun selectAllCaregivers(type: MeasurementType) {
        val ids = latestCaregivers.map { it.id }
        disposables.add(
            updateAlertCaregivers(type, ids)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }

    fun deselectAllCaregivers(type: MeasurementType) {
        disposables.add(
            updateAlertCaregivers(type, emptyList())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }

    fun savePressureConfig(
        systolicMin: Double?,
        systolicMax: Double?,
        diastolicMin: Double?,
        diastolicMax: Double?
    ) {
        disposables.add(
            updateBloodPressureCriticalThresholds(
                systolicMin = systolicMin,
                systolicMax = systolicMax,
                diastolicMin = diastolicMin,
                diastolicMax = diastolicMax
            ).subscribeOn(Schedulers.io())
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
    val diastolicMax: Double?
) {
    companion object {
        fun fallback(): PressureAlertConfigUi = PressureAlertConfigUi(
            enabled = true,
            systolicMin = null,
            systolicMax = null,
            diastolicMin = null,
            diastolicMax = null
        )
    }
}

data class AlertNotificationsUi(
    val enabled: Boolean,
    val channels: Set<AlertChannel>,
    val caregivers: List<CaregiverUi>
) {
    data class CaregiverUi(
        val id: String,
        val fullName: String,
        val phone: String,
        val selected: Boolean
    )
}

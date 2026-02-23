package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import zdrowy.senior.io.domain.agent.Agent
import zdrowy.senior.io.domain.agent.AgentRole
import zdrowy.senior.io.domain.agent.ObserveAgentsUseCase
import zdrowy.senior.io.domain.alert.AlertChannel
import zdrowy.senior.io.domain.alert.AlertSetting
import zdrowy.senior.io.domain.settings.notifications.ObserveNotificationSettingsUseCase
import zdrowy.senior.io.domain.settings.notifications.SetAlertEnabledUseCase
import zdrowy.senior.io.domain.settings.notifications.UpdateAlertCaregiversUseCase
import zdrowy.senior.io.domain.settings.notifications.UpdateAlertChannelsUseCase
import zdrowy.senior.io.domain.settings.notifications.UpdateBloodPressureCriticalThresholdsUseCase
import zdrowy.senior.io.domain.settings.notifications.UpdateCriticalThresholdsUseCase
import zdrowy.senior.io.domain.settings.notifications.UpdateSugarDropRulesUseCase
import zdrowy.senior.io.domain.measurement.MeasurementType
import java.util.concurrent.TimeUnit

// Domyślne granice "prawidłowego" ciśnienia (office, ESC/ESH):
// - hipotonia często: <90/<60
// - górna granica "normal": <130/<85
private const val DEFAULT_SYS_MIN = 90.0
private const val DEFAULT_SYS_MAX = 129.0
private const val DEFAULT_DIA_MIN = 60.0
private const val DEFAULT_DIA_MAX = 84.0

class PatientAlertsViewModel(
    private val observeNotificationSettings: ObserveNotificationSettingsUseCase,
    private val setAlertEnabled: SetAlertEnabledUseCase,
    private val updateBloodPressureCriticalThresholds: UpdateBloodPressureCriticalThresholdsUseCase,
    private val updateCriticalThresholds: UpdateCriticalThresholdsUseCase,
    private val updateSugarDropRules: UpdateSugarDropRulesUseCase,
    private val updateAlertChannels: UpdateAlertChannelsUseCase,
    private val updateAlertCaregivers: UpdateAlertCaregiversUseCase,
    private val observeAgents: ObserveAgentsUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val configDisposable = SerialDisposable()
    private val pressureDrafts = PublishSubject.create<PressureDraft>()
    private val sugarDrafts = PublishSubject.create<SugarDraft>()
    private val pulseDrafts = PublishSubject.create<PulseDraft>()
    private var latestSettings: Map<MeasurementType, AlertSetting> = emptyMap()
    private var latestCaregivers: List<Agent> = emptyList()

    private val _pressureConfig = MutableLiveData<PressureAlertConfigUi>()
    val pressureConfig: LiveData<PressureAlertConfigUi> = _pressureConfig

    private val _sugarConfig = MutableLiveData<SugarAlertConfigUi>()
    val sugarConfig: LiveData<SugarAlertConfigUi> = _sugarConfig

    private val _pulseConfig = MutableLiveData<PulseAlertConfigUi>()
    val pulseConfig: LiveData<PulseAlertConfigUi> = _pulseConfig

    private val _notifications = MutableLiveData<Map<MeasurementType, AlertNotificationsUi>>()
    val notifications: LiveData<Map<MeasurementType, AlertNotificationsUi>> = _notifications

    init {
        disposables.add(configDisposable)
        bindAutoSave()
    }

    fun start() {
        configDisposable.set(
            Observable.combineLatest(
                observeNotificationSettings(),
                observeAgents().map { agents -> agents.filter { it.role == AgentRole.CAREGIVER } }
            ) { settings, caregivers -> Pair(settings, caregivers) }
                .subscribeOn(Schedulers.io())
                .map { (settings, caregivers) ->
                    latestSettings = settings.alerts.associateBy { it.type }
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

                    val setting = settings.alerts.firstOrNull { it.type == MeasurementType.PRESSURE }
                    val pressureUi = if (setting == null) {
                        PressureAlertConfigUi.fallback()
                    } else {
                        val sysMin = setting.systolicMin
                        val sysMax = setting.systolicMax
                        val diaMin = setting.diastolicMin
                        val diaMax = setting.diastolicMax

                        val resolvedSysMin = if (sysMin == null && sysMax == null) DEFAULT_SYS_MIN else sysMin
                        val resolvedSysMax = if (sysMin == null && sysMax == null) DEFAULT_SYS_MAX else sysMax
                        val resolvedDiaMin = if (diaMin == null && diaMax == null) DEFAULT_DIA_MIN else diaMin
                        val resolvedDiaMax = if (diaMin == null && diaMax == null) DEFAULT_DIA_MAX else diaMax

                        PressureAlertConfigUi(
                            enabled = setting.enabled,
                            systolicMin = resolvedSysMin,
                            systolicMax = resolvedSysMax,
                            diastolicMin = resolvedDiaMin,
                            diastolicMax = resolvedDiaMax
                        )
                    }

                    val sugarSetting = settings.alerts.firstOrNull { it.type == MeasurementType.SUGAR }
                    val sugarUi = SugarAlertConfigUi(
                        enabled = sugarSetting?.enabled ?: true,
                        min = sugarSetting?.min,
                        max = sugarSetting?.max,
                        dropDelta = sugarSetting?.dropDelta,
                        dropWindowMinutes = sugarSetting?.dropWindowMinutes
                    )

                    val pulseSetting = settings.alerts.firstOrNull { it.type == MeasurementType.PULSE }
                    val pulseUi = PulseAlertConfigUi(
                        enabled = pulseSetting?.enabled ?: true,
                        min = pulseSetting?.min,
                        max = pulseSetting?.max
                    )

                    AlertsUiSnapshot(
                        pressure = pressureUi,
                        sugar = sugarUi,
                        pulse = pulseUi,
                        notifications = notifications
                    )
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ snapshot ->
                    _pressureConfig.value = snapshot.pressure
                    _sugarConfig.value = snapshot.sugar
                    _pulseConfig.value = snapshot.pulse
                    _notifications.value = snapshot.notifications
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

    fun onPressureFieldsChanged(
        systolicMin: Double?,
        systolicMax: Double?,
        diastolicMin: Double?,
        diastolicMax: Double?
    ) {
        pressureDrafts.onNext(PressureDraft(systolicMin, systolicMax, diastolicMin, diastolicMax))
    }

    fun onSugarFieldsChanged(
        min: Double?,
        max: Double?,
        dropDelta: Double?,
        dropWindowMinutes: Int?
    ) {
        sugarDrafts.onNext(SugarDraft(min, max, dropDelta, dropWindowMinutes))
    }

    fun onPulseFieldsChanged(min: Double?, max: Double?) {
        pulseDrafts.onNext(PulseDraft(min, max))
    }

    private fun bindAutoSave() {
        disposables.add(
            pressureDrafts
                .debounce(600, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .flatMapCompletable { draft ->
                    updateBloodPressureCriticalThresholds(
                        systolicMin = draft.systolicMin,
                        systolicMax = draft.systolicMax,
                        diastolicMin = draft.diastolicMin,
                        diastolicMax = draft.diastolicMax
                    ).subscribeOn(Schedulers.io())
                }
                .subscribe({}, {})
        )
        disposables.add(
            sugarDrafts
                .debounce(600, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .flatMapCompletable { draft ->
                    updateCriticalThresholds(
                        MeasurementType.SUGAR,
                        draft.min,
                        draft.max
                    ).andThen(updateSugarDropRules(draft.dropDelta, draft.dropWindowMinutes))
                        .subscribeOn(Schedulers.io())
                }
                .subscribe({}, {})
        )
        disposables.add(
            pulseDrafts
                .debounce(600, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .flatMapCompletable { draft ->
                    updateCriticalThresholds(
                        MeasurementType.PULSE,
                        draft.min,
                        draft.max
                    ).subscribeOn(Schedulers.io())
                }
                .subscribe({}, {})
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}

private data class AlertsUiSnapshot(
    val pressure: PressureAlertConfigUi,
    val sugar: SugarAlertConfigUi,
    val pulse: PulseAlertConfigUi,
    val notifications: Map<MeasurementType, AlertNotificationsUi>
)

private data class PressureDraft(
    val systolicMin: Double?,
    val systolicMax: Double?,
    val diastolicMin: Double?,
    val diastolicMax: Double?
)

private data class SugarDraft(
    val min: Double?,
    val max: Double?,
    val dropDelta: Double?,
    val dropWindowMinutes: Int?
)

private data class PulseDraft(
    val min: Double?,
    val max: Double?
)

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
            systolicMin = DEFAULT_SYS_MIN,
            systolicMax = DEFAULT_SYS_MAX,
            diastolicMin = DEFAULT_DIA_MIN,
            diastolicMax = DEFAULT_DIA_MAX
        )
    }
}

data class SugarAlertConfigUi(
    val enabled: Boolean,
    val min: Double?,
    val max: Double?,
    val dropDelta: Double?,
    val dropWindowMinutes: Int?
)

data class PulseAlertConfigUi(
    val enabled: Boolean,
    val min: Double?,
    val max: Double?
)

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

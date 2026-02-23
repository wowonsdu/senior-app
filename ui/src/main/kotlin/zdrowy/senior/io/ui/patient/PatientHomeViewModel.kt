package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.alert.TriggerBloodPressureAlertEventUseCase
import zdrowy.senior.io.domain.measurement.AddBloodPressureMeasurementUseCase
import zdrowy.senior.io.domain.measurement.AddMeasurementUseCase
import zdrowy.senior.io.domain.measurement.Measurement
import zdrowy.senior.io.domain.measurement.MeasurementSource
import zdrowy.senior.io.domain.measurement.MeasurementType
import zdrowy.senior.io.domain.measurement.ObserveRecentMeasurementsUseCase
import zdrowy.senior.io.domain.measurement.ParseVoiceMeasurementsUseCase
import zdrowy.senior.io.domain.measurement.ParsedVoiceMeasurement
import zdrowy.senior.io.domain.settings.ObservePersonalDataByUidUseCase
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.domain.user.ManagedUserUidState
import zdrowy.senior.io.domain.user.ObserveManagedUserUidStateUseCase
import zdrowy.senior.io.domain.user.UserRole
import zdrowy.senior.io.ui.R

class PatientHomeViewModel(
    private val observeRecentMeasurements: ObserveRecentMeasurementsUseCase,
    private val observeManagedUserUidState: ObserveManagedUserUidStateUseCase,
    private val observePersonalDataByUid: ObservePersonalDataByUidUseCase,
    private val parseVoiceMeasurements: ParseVoiceMeasurementsUseCase,
    private val addMeasurement: AddMeasurementUseCase,
    private val addBloodPressureMeasurement: AddBloodPressureMeasurementUseCase,
    private val triggerBloodPressureAlertEvent: TriggerBloodPressureAlertEventUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _recentMeasurements = MutableLiveData<List<Measurement>>()
    val recentMeasurements: LiveData<List<Measurement>> = _recentMeasurements
    private val _headerState = MutableLiveData<PatientHomeHeaderUiState>()
    val headerState: LiveData<PatientHomeHeaderUiState> = _headerState
    private val _navTarget = MutableLiveData<PatientHomeNavTarget?>()
    val navTarget: LiveData<PatientHomeNavTarget?> = _navTarget
    private val _voiceSummary = MutableLiveData<VoiceSaveSummary?>()
    val voiceSummary: LiveData<VoiceSaveSummary?> = _voiceSummary
    private var started = false

    fun start() {
        if (started) return
        started = true
        disposables.add(
            observeManagedUserUidState()
                .subscribeOn(Schedulers.io())
                .switchMap { state ->
                    when (state) {
                        is ManagedUserUidState.Available ->
                            observePersonalDataByUid(state.uid)
                                .map { data -> mapHeaderState(state.role, data) }
                        ManagedUserUidState.MissingActivePatient ->
                            Observable.just(missingPatientHeaderState())
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ header ->
                    _headerState.value = header
                }, {
                    _headerState.value = fallbackHeaderState()
                })
        )
        disposables.add(
            observeManagedUserUidState()
                .subscribeOn(Schedulers.io())
                .switchMap { state ->
                    when (state) {
                        is ManagedUserUidState.Available ->
                            observeRecentMeasurements(4)
                        ManagedUserUidState.MissingActivePatient ->
                            Observable.just(emptyList())
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ measurements ->
                    _recentMeasurements.value = measurements
                }, {
                    _recentMeasurements.value = emptyList()
                })
        )
    }

    fun onHeaderClicked() {
        when (_headerState.value?.action ?: PatientHomeHeaderAction.NONE) {
            PatientHomeHeaderAction.LINK_CAREGIVER -> {
                _navTarget.value = PatientHomeNavTarget.CAREGIVER_LINK
            }
            PatientHomeHeaderAction.BACK_TO_CAREGIVER -> {
                _navTarget.value = PatientHomeNavTarget.CAREGIVER_HOME
            }
            PatientHomeHeaderAction.NONE -> Unit
        }
    }

    fun onNavigationHandled() {
        _navTarget.value = null
    }

    fun onVoiceText(text: String) {
        val result = parseVoiceMeasurements(text)
        val savedTypes = result.measurements.map { it.type }.distinct()
        val skippedTypes = result.skippedTypes.distinct()

        if (result.measurements.isEmpty()) {
            _voiceSummary.value = VoiceSaveSummary(savedTypes, skippedTypes)
            return
        }

        val timestamp = System.currentTimeMillis()
        val tasks = result.measurements.mapNotNull { measurement ->
            buildSaveTask(measurement, timestamp)
        }
        val save = if (tasks.isEmpty()) {
            Completable.complete()
        } else {
            Completable.merge(tasks)
        }
        disposables.add(
            save.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _voiceSummary.value = VoiceSaveSummary(savedTypes, skippedTypes)
                }, {
                    _voiceSummary.value = VoiceSaveSummary(savedTypes, skippedTypes)
                })
        )
    }

    fun onVoiceSummaryHandled() {
        _voiceSummary.value = null
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    private fun buildSaveTask(
        measurement: ParsedVoiceMeasurement,
        timestamp: Long
    ): Completable? {
        return if (measurement.type == MeasurementType.PRESSURE) {
            val systolic = measurement.systolic ?: return null
            val diastolic = measurement.diastolic ?: return null
            addBloodPressureMeasurement(
                systolic = systolic,
                diastolic = diastolic,
                timestamp = timestamp,
                source = MeasurementSource.VOICE
            ).flatMapCompletable { id ->
                triggerBloodPressureAlertEvent(id, systolic, diastolic, timestamp)
                    .onErrorComplete()
            }
        } else {
            val value = measurement.value ?: return null
            addMeasurement(
                type = measurement.type,
                value = value,
                timestamp = timestamp,
                source = MeasurementSource.VOICE
            ).ignoreElement()
        }
    }

    private fun mapHeaderState(role: UserRole, data: PersonalData): PatientHomeHeaderUiState {
        val firstName = data.firstName.trim()
        val lastName = data.lastName.trim()
        val fullName = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
        val phone = data.phoneNumber.trim().ifBlank { "-" }
        val labelRes = if (role == UserRole.PATIENT) {
            R.string.patient_home_profile_label_patient
        } else {
            R.string.patient_home_profile_label
        }
        val action = if (role == UserRole.CAREGIVER) {
            PatientHomeHeaderAction.BACK_TO_CAREGIVER
        } else {
            PatientHomeHeaderAction.NONE
        }
        val actionIconRes = if (role == UserRole.CAREGIVER) {
            R.drawable.ic_exit_door
        } else {
            R.drawable.ic_chevron_down
        }
        return PatientHomeHeaderUiState(
            labelRes = labelRes,
            fullName = if (fullName.isBlank()) "-" else fullName,
            phone = phone,
            avatar = initials(firstName, lastName),
            action = action,
            actionIconRes = actionIconRes
        )
    }

    private fun missingPatientHeaderState(): PatientHomeHeaderUiState {
        return PatientHomeHeaderUiState(
            labelRes = R.string.patient_home_profile_label_missing_patient,
            fullName = "-",
            phone = "-",
            avatar = "?",
            action = PatientHomeHeaderAction.LINK_CAREGIVER,
            actionIconRes = R.drawable.ic_chevron_down
        )
    }

    private fun fallbackHeaderState(): PatientHomeHeaderUiState {
        return PatientHomeHeaderUiState(
            labelRes = R.string.patient_home_profile_label,
            fullName = "-",
            phone = "-",
            avatar = "?",
            action = PatientHomeHeaderAction.NONE,
            actionIconRes = R.drawable.ic_chevron_down
        )
    }

    private fun initials(firstName: String, lastName: String): String {
        val first = firstName.firstOrNull()?.uppercaseChar()
        val last = lastName.firstOrNull()?.uppercaseChar()
        val value = buildString {
            if (first != null) append(first)
            if (last != null) append(last)
        }
        return if (value.isBlank()) "?" else value
    }
}

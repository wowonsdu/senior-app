package zdrowy.senior.io.ui.caregiver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.measurement.Measurement
import zdrowy.senior.io.domain.measurement.MeasurementType
import zdrowy.senior.io.domain.measurement.ObserveMeasurementReadStateUseCase
import zdrowy.senior.io.domain.measurement.ObserveRecentMeasurementsUseCase
import zdrowy.senior.io.domain.measurement.SetMeasurementReadStateUseCase
import zdrowy.senior.io.domain.settings.ObservePersonalDataByUidUseCase
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.domain.user.ManagedUserUidState
import zdrowy.senior.io.domain.user.ObserveManagedUserUidStateUseCase
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.caregiver.model.CaregiverMeasurementNotificationUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CaregiverDashboardViewModel(
    private val observeManagedUserUidState: ObserveManagedUserUidStateUseCase,
    private val observeRecentMeasurements: ObserveRecentMeasurementsUseCase,
    private val observeMeasurementReadState: ObserveMeasurementReadStateUseCase,
    private val setMeasurementReadState: SetMeasurementReadStateUseCase,
    private val observePersonalDataByUid: ObservePersonalDataByUidUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _uiState = MutableLiveData<CaregiverDashboardUiState>()
    val uiState: LiveData<CaregiverDashboardUiState> = _uiState
    private var started = false
    private var selectedTab = CaregiverDashboardTab.NEW
    private var currentPatientUid: String? = null
    private var latestMeasurements: List<Measurement> = emptyList()
    private var latestNewItems: List<CaregiverMeasurementNotificationUi> = emptyList()
    private var latestReadItems: List<CaregiverMeasurementNotificationUi> = emptyList()
    private var missingPatient = false

    fun start() {
        if (started) return
        started = true
        disposables.add(
            observeManagedUserUidState()
                .subscribeOn(Schedulers.io())
                .switchMap { state ->
                    when (state) {
                        is ManagedUserUidState.Available -> {
                            currentPatientUid = state.uid
                            missingPatient = false
                            Observable.combineLatest(
                                observeRecentMeasurements(LATEST_LIMIT),
                                observeMeasurementReadState(state.uid),
                                observePersonalDataByUid(state.uid)
                                    .onErrorReturnItem(fallbackPersonalData())
                            ) { measurements, readState, personalData ->
                                DashboardData(
                                    patientUid = state.uid,
                                    measurements = measurements,
                                    lastReadAtMs = readState.lastReadAtMs,
                                    patientName = mapPatientName(personalData),
                                    missingPatient = false
                                )
                            }
                        }
                        ManagedUserUidState.MissingActivePatient -> {
                            currentPatientUid = null
                            Observable.just(
                                DashboardData(
                                    patientUid = "",
                                    measurements = emptyList(),
                                    lastReadAtMs = 0L,
                                    patientName = "-",
                                    missingPatient = true
                                )
                            )
                        }
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data ->
                    applyData(data)
                }, {
                    applyData(
                        DashboardData(
                            patientUid = currentPatientUid.orEmpty(),
                            measurements = emptyList(),
                            lastReadAtMs = 0L,
                            patientName = "-",
                            missingPatient = missingPatient
                        )
                    )
                })
        )
    }

    fun setTab(tab: CaregiverDashboardTab) {
        if (selectedTab == tab) return
        selectedTab = tab
        publishState()
    }

    fun markRead(item: CaregiverMeasurementNotificationUi) {
        val uid = currentPatientUid ?: return
        disposables.add(
            setMeasurementReadState(uid, item.timestamp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }

    fun markAllRead() {
        val uid = currentPatientUid ?: return
        val latestTimestamp = latestMeasurements.maxOfOrNull { it.timestamp } ?: return
        disposables.add(
            setMeasurementReadState(uid, latestTimestamp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    private fun applyData(data: DashboardData) {
        latestMeasurements = data.measurements
        missingPatient = data.missingPatient
        val items = mapItems(data.patientName, data.measurements, data.lastReadAtMs)
        latestNewItems = items.first
        latestReadItems = items.second
        publishState()
    }

    private fun publishState() {
        _uiState.value = CaregiverDashboardUiState(
            selectedTab = selectedTab,
            newItems = latestNewItems,
            readItems = latestReadItems,
            showMissingPatient = missingPatient
        )
    }

    private fun mapItems(
        patientName: String,
        measurements: List<Measurement>,
        lastReadAtMs: Long
    ): Pair<List<CaregiverMeasurementNotificationUi>, List<CaregiverMeasurementNotificationUi>> {
        val formatter = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
        val items = measurements.map { measurement ->
            val isRead = measurement.timestamp <= lastReadAtMs
            CaregiverMeasurementNotificationUi(
                id = measurement.id,
                patientName = patientName,
                type = measurement.type,
                typeLabel = typeLabel(measurement.type),
                valueLabel = valueLabel(measurement),
                timeLabel = formatter.format(Date(measurement.timestamp)),
                iconRes = iconFor(measurement.type),
                iconTintRes = tintFor(measurement.type),
                chipColorRes = tintFor(measurement.type),
                timestamp = measurement.timestamp,
                isRead = isRead
            )
        }
        val newItems = items.filter { !it.isRead }
        val readItems = items.filter { it.isRead }
        return newItems to readItems
    }

    private fun typeLabel(type: MeasurementType): String {
        return when (type) {
            MeasurementType.SUGAR -> "cukier"
            MeasurementType.INSULIN -> "insulina"
            MeasurementType.PRESSURE -> "cisnienie"
            MeasurementType.PULSE -> "tetno"
        }
    }

    private fun valueLabel(measurement: Measurement): String {
        return when (measurement.type) {
            MeasurementType.PRESSURE -> {
                val systolic = measurement.systolic ?: 0
                val diastolic = measurement.diastolic ?: 0
                "Wartosc: ${systolic}/${diastolic} mmHg"
            }
            MeasurementType.SUGAR -> "Wartosc: ${measurement.value ?: 0.0} mg/dl"
            MeasurementType.INSULIN -> "Wartosc: ${measurement.value ?: 0.0} j."
            MeasurementType.PULSE -> "Wartosc: ${measurement.value ?: 0.0} bpm"
        }
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

    private fun mapPatientName(data: PersonalData): String {
        val firstName = data.firstName.trim()
        val lastName = data.lastName.trim()
        val fullName = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
        return if (fullName.isBlank()) "-" else fullName
    }

    private fun fallbackPersonalData(): PersonalData {
        return PersonalData(
            firstName = "",
            lastName = "",
            pesel = "",
            phoneNumber = "",
            email = "",
            address = ""
        )
    }

    private data class DashboardData(
        val patientUid: String,
        val measurements: List<Measurement>,
        val lastReadAtMs: Long,
        val patientName: String,
        val missingPatient: Boolean
    )

    private companion object {
        private const val LATEST_LIMIT = 50
    }
}

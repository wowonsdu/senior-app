package zdrowy.senior.io.ui.caregiver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.carelink.CareLinkStatus
import zdrowy.senior.io.domain.carelink.ObserveCareLinksUseCase
import zdrowy.senior.io.domain.measurement.Measurement
import zdrowy.senior.io.domain.measurement.MeasurementType
import zdrowy.senior.io.domain.measurement.MarkMeasurementReadUseCase
import zdrowy.senior.io.domain.measurement.MeasurementReadState
import zdrowy.senior.io.domain.measurement.ObserveMeasurementReadStateUseCase
import zdrowy.senior.io.domain.measurement.ObserveRecentMeasurementsByUidUseCase
import zdrowy.senior.io.domain.measurement.SetReadMeasurementsUseCase
import zdrowy.senior.io.domain.settings.ObservePersonalDataByUidUseCase
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.caregiver.model.CaregiverMeasurementNotificationUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CaregiverDashboardViewModel(
    private val observeCareLinks: ObserveCareLinksUseCase,
    private val observeRecentMeasurementsByUid: ObserveRecentMeasurementsByUidUseCase,
    private val observeMeasurementReadState: ObserveMeasurementReadStateUseCase,
    private val markMeasurementRead: MarkMeasurementReadUseCase,
    private val setReadMeasurements: SetReadMeasurementsUseCase,
    private val observePersonalDataByUid: ObservePersonalDataByUidUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _uiState = MutableLiveData<CaregiverDashboardUiState>()
    val uiState: LiveData<CaregiverDashboardUiState> = _uiState
    private val dateFormatter = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
    private var started = false
    private var selectedTab = CaregiverDashboardTab.NEW
    private var latestNewItems: List<CaregiverMeasurementNotificationUi> = emptyList()
    private var latestReadItems: List<CaregiverMeasurementNotificationUi> = emptyList()
    private var missingPatients = false

    fun start() {
        if (started) return
        started = true
        disposables.add(
            observeCareLinks()
                .subscribeOn(Schedulers.io())
                .switchMap { links ->
                    val patientUids = links
                        .filter { it.status == CareLinkStatus.ACTIVE }
                        .map { it.patientUid }
                        .distinct()
                    if (patientUids.isEmpty()) {
                        return@switchMap Observable.just(emptyList<PatientDashboardData>())
                    }
                    val streams = patientUids.map { patientUid ->
                        Observable.combineLatest(
                            observeRecentMeasurementsByUid(patientUid, LATEST_LIMIT_PER_PATIENT),
                            observeMeasurementReadState(patientUid)
                                .onErrorReturnItem(emptyReadState(patientUid)),
                            observePersonalDataByUid(patientUid)
                                .onErrorReturnItem(fallbackPersonalData())
                        ) { measurements, readState, personalData ->
                            PatientDashboardData(
                                patientUid = patientUid,
                                measurements = measurements,
                                readIds = readState.readMeasurementIds,
                                patientName = mapPatientName(personalData)
                            )
                        }
                    }
                    Observable.combineLatest(streams) { items ->
                        items.map { it as PatientDashboardData }
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ dataList ->
                    applyData(dataList)
                }, {
                    applyData(emptyList())
                })
        )
    }

    fun setTab(tab: CaregiverDashboardTab) {
        if (selectedTab == tab) return
        selectedTab = tab
        publishState()
    }

    fun markRead(item: CaregiverMeasurementNotificationUi) {
        disposables.add(
            markMeasurementRead(item.patientUid, item.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }

    fun markAllRead() {
        val tasks = latestNewItems
            .groupBy { it.patientUid }
            .mapNotNull { (uid, items) ->
                val ids = items.map { it.id }.filter { it.isNotBlank() }
                if (ids.isEmpty()) null else setReadMeasurements(uid, ids)
            }
        if (tasks.isEmpty()) return
        disposables.add(
            Completable.merge(tasks)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    private fun applyData(data: List<PatientDashboardData>) {
        missingPatients = data.isEmpty()
        val items = data.flatMap { patient ->
            patient.measurements.map { measurement ->
                toUiItem(patient, measurement)
            }
        }
        val limited = items.sortedByDescending { it.timestamp }.take(LATEST_LIMIT_TOTAL)
        latestNewItems = limited.filter { !it.isRead }
        latestReadItems = limited.filter { it.isRead }
        publishState()
    }

    private fun publishState() {
        _uiState.value = CaregiverDashboardUiState(
            selectedTab = selectedTab,
            newItems = latestNewItems,
            readItems = latestReadItems,
            showMissingPatient = missingPatients
        )
    }

    private fun toUiItem(
        patient: PatientDashboardData,
        measurement: Measurement
    ): CaregiverMeasurementNotificationUi {
        val isRead = patient.readIds.contains(measurement.id)
        return CaregiverMeasurementNotificationUi(
            id = measurement.id,
            patientUid = patient.patientUid,
            patientName = patient.patientName,
            type = measurement.type,
            typeLabel = typeLabel(measurement.type),
            valueLabel = valueLabel(measurement),
            timeLabel = dateFormatter.format(Date(measurement.timestamp)),
            iconRes = iconFor(measurement.type),
            iconTintRes = tintFor(measurement.type),
            chipColorRes = tintFor(measurement.type),
            timestamp = measurement.timestamp,
            isRead = isRead
        )
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

    private fun emptyReadState(patientUid: String) =
        MeasurementReadState(patientUid, emptySet())

    private data class PatientDashboardData(
        val patientUid: String,
        val measurements: List<Measurement>,
        val readIds: Set<String>,
        val patientName: String
    )

    private companion object {
        private const val LATEST_LIMIT_PER_PATIENT = 20
        private const val LATEST_LIMIT_TOTAL = 50
    }
}

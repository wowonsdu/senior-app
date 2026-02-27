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
import zdrowy.senior.io.domain.settings.reminders.MedicationReminderEvent
import zdrowy.senior.io.domain.settings.reminders.MedicationReminderReadState
import zdrowy.senior.io.domain.settings.reminders.MarkMedicationReminderReadUseCase
import zdrowy.senior.io.domain.settings.reminders.ObserveMedicationReminderEventsUseCase
import zdrowy.senior.io.domain.settings.reminders.ObserveMedicationReminderReadStateUseCase
import zdrowy.senior.io.domain.settings.reminders.SetReadMedicationRemindersUseCase
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.caregiver.model.CaregiverDashboardItemKind
import zdrowy.senior.io.ui.caregiver.model.CaregiverDashboardItemUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CaregiverDashboardViewModel(
    private val observeCareLinks: ObserveCareLinksUseCase,
    private val observeRecentMeasurementsByUid: ObserveRecentMeasurementsByUidUseCase,
    private val observeMeasurementReadState: ObserveMeasurementReadStateUseCase,
    private val markMeasurementRead: MarkMeasurementReadUseCase,
    private val setReadMeasurements: SetReadMeasurementsUseCase,
    private val observePersonalDataByUid: ObservePersonalDataByUidUseCase,
    private val observeMedicationReminderEvents: ObserveMedicationReminderEventsUseCase,
    private val observeMedicationReminderReadState: ObserveMedicationReminderReadStateUseCase,
    private val markMedicationReminderRead: MarkMedicationReminderReadUseCase,
    private val setReadMedicationReminders: SetReadMedicationRemindersUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _uiState = MutableLiveData<CaregiverDashboardUiState>()
    val uiState: LiveData<CaregiverDashboardUiState> = _uiState
    private val dateFormatter = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
    private var started = false
    private var selectedTab = CaregiverDashboardTab.NEW
    private var latestNewItems: List<CaregiverDashboardItemUi> = emptyList()
    private var latestReadItems: List<CaregiverDashboardItemUi> = emptyList()
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
                                .onErrorReturnItem(fallbackPersonalData()),
                            observeMedicationReminderEvents(patientUid, REMINDER_LIMIT_PER_PATIENT)
                                .onErrorReturnItem(emptyList()),
                            observeMedicationReminderReadState(patientUid)
                                .onErrorReturnItem(emptyReminderReadState(patientUid))
                        ) { measurements, readState, personalData, reminders, reminderReadState ->
                            PatientDashboardData(
                                patientUid = patientUid,
                                measurements = measurements,
                                readIds = readState.readMeasurementIds,
                                patientName = mapPatientName(personalData),
                                medicationEvents = reminders,
                                medicationReadIds = reminderReadState.readEventIds
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

    fun markRead(item: CaregiverDashboardItemUi) {
        when (item.kind) {
            CaregiverDashboardItemKind.MEASUREMENT -> {
                disposables.add(
                    markMeasurementRead(item.patientUid, item.id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({}, {})
                )
            }
            CaregiverDashboardItemKind.MEDICATION -> {
                disposables.add(
                    markMedicationReminderRead(item.patientUid, item.id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({}, {})
                )
            }
        }
    }

    fun markAllRead() {
        val measurementTasks = latestNewItems
            .filter { it.kind == CaregiverDashboardItemKind.MEASUREMENT }
            .groupBy { it.patientUid }
            .mapNotNull { (uid, items) ->
                val ids = items.map { it.id }.filter { it.isNotBlank() }
                if (ids.isEmpty()) null else setReadMeasurements(uid, ids)
            }
        val reminderTasks = latestNewItems
            .filter { it.kind == CaregiverDashboardItemKind.MEDICATION }
            .groupBy { it.patientUid }
            .mapNotNull { (uid, items) ->
                val ids = items.map { it.id }.filter { it.isNotBlank() }
                if (ids.isEmpty()) null else setReadMedicationReminders(uid, ids)
            }
        val tasks = measurementTasks + reminderTasks
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
            val measurementItems = patient.measurements.map { measurement ->
                toMeasurementUiItem(patient, measurement)
            }
            val reminderItems = patient.medicationEvents.map { event ->
                toMedicationUiItem(patient, event)
            }
            measurementItems + reminderItems
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

    private fun toMeasurementUiItem(
        patient: PatientDashboardData,
        measurement: Measurement
    ): CaregiverDashboardItemUi {
        val isRead = patient.readIds.contains(measurement.id)
        return CaregiverDashboardItemUi(
            id = measurement.id,
            patientUid = patient.patientUid,
            patientName = patient.patientName,
            typeLabel = typeLabel(measurement.type),
            valueLabel = valueLabel(measurement),
            timeLabel = dateFormatter.format(Date(measurement.timestamp)),
            iconRes = iconFor(measurement.type),
            iconTintRes = tintFor(measurement.type),
            chipColorRes = tintFor(measurement.type),
            timestamp = measurement.timestamp,
            isRead = isRead,
            kind = CaregiverDashboardItemKind.MEASUREMENT
        )
    }

    private fun toMedicationUiItem(
        patient: PatientDashboardData,
        event: MedicationReminderEvent
    ): CaregiverDashboardItemUi {
        val isRead = patient.medicationReadIds.contains(event.id)
        val baseLabel = if (event.dosage.isBlank()) {
            "Lek: ${event.medicationName}"
        } else {
            "Lek: ${event.medicationName}, dawka: ${event.dosage}"
        }
        val label = if (event.isTaken) {
            val takenTime = event.takenAtMs?.let { takenMs ->
                dateFormatter.format(Date(takenMs))
            } ?: "-"
            "$baseLabel | status: wziety ($takenTime)"
        } else {
            "$baseLabel | status: oczekuje"
        }
        return CaregiverDashboardItemUi(
            id = event.id,
            patientUid = patient.patientUid,
            patientName = patient.patientName,
            typeLabel = "lek",
            valueLabel = label,
            timeLabel = dateFormatter.format(Date(event.scheduledAtMs)),
            iconRes = R.drawable.ic_alert_bell,
            iconTintRes = R.color.senior_info,
            chipColorRes = R.color.senior_info,
            timestamp = event.scheduledAtMs,
            isRead = isRead,
            kind = CaregiverDashboardItemKind.MEDICATION
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

    private fun emptyReminderReadState(patientUid: String) =
        MedicationReminderReadState(patientUid, emptySet())

    private data class PatientDashboardData(
        val patientUid: String,
        val measurements: List<Measurement>,
        val readIds: Set<String>,
        val patientName: String,
        val medicationEvents: List<MedicationReminderEvent>,
        val medicationReadIds: Set<String>
    )

    private companion object {
        private const val LATEST_LIMIT_PER_PATIENT = 20
        private const val REMINDER_LIMIT_PER_PATIENT = 20
        private const val LATEST_LIMIT_TOTAL = 50
    }
}

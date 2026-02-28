package zdrowy.senior.io.ui.caregiver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.carelink.ObserveCareLinksUseCase
import zdrowy.senior.io.domain.carelink.CareLinkCodeType
import zdrowy.senior.io.domain.carelink.GenerateCareLinkCodeUseCase
import zdrowy.senior.io.domain.measurement.Measurement
import zdrowy.senior.io.domain.measurement.MeasurementReadState
import zdrowy.senior.io.domain.measurement.ObserveMeasurementReadStateUseCase
import zdrowy.senior.io.domain.measurement.ObserveRecentMeasurementsByUidUseCase
import zdrowy.senior.io.domain.settings.reminders.MedicationReminderEvent
import zdrowy.senior.io.domain.settings.reminders.MedicationReminderReadState
import zdrowy.senior.io.domain.settings.reminders.ObserveCaregiverMedicationReminderPrefsUseCase
import zdrowy.senior.io.domain.settings.reminders.ObserveMedicationReminderEventsUseCase
import zdrowy.senior.io.domain.settings.reminders.ObserveMedicationReminderReadStateUseCase
import zdrowy.senior.io.domain.settings.reminders.SetCaregiverMedicationReminderPrefUseCase
import zdrowy.senior.io.domain.settings.ObservePersonalDataByUidUseCase
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.domain.user.CurrentUserUidProvider
import zdrowy.senior.io.domain.user.SetActivePatientUseCase
import zdrowy.senior.io.ui.caregiver.model.CaregiverDependentTileUiModel

class CaregiverDependentsViewModel(
    private val observeCareLinks: ObserveCareLinksUseCase,
    private val generateCareLinkCode: GenerateCareLinkCodeUseCase,
    private val observePersonalDataByUid: ObservePersonalDataByUidUseCase,
    private val observeRecentMeasurementsByUid: ObserveRecentMeasurementsByUidUseCase,
    private val observeMeasurementReadState: ObserveMeasurementReadStateUseCase,
    private val observeMedicationReminderEvents: ObserveMedicationReminderEventsUseCase,
    private val observeMedicationReminderReadState: ObserveMedicationReminderReadStateUseCase,
    private val observeCaregiverPrefs: ObserveCaregiverMedicationReminderPrefsUseCase,
    private val setCaregiverPref: SetCaregiverMedicationReminderPrefUseCase,
    private val setActivePatient: SetActivePatientUseCase,
    private val currentUserUidProvider: CurrentUserUidProvider
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _dependents = MutableLiveData<List<CaregiverDependentTileUiModel>>()
    val dependents: LiveData<List<CaregiverDependentTileUiModel>> = _dependents
    private val _navTarget = MutableLiveData<String?>()
    val navTarget: LiveData<String?> = _navTarget
    private val _linkCodeToCopy = MutableLiveData<String?>()
    val linkCodeToCopy: LiveData<String?> = _linkCodeToCopy
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message
    private var started = false

    fun start() {
        if (started) return
        started = true
        disposables.add(
            observeCareLinks()
                .subscribeOn(Schedulers.io())
                .switchMap { links ->
                    val selfUid = try {
                        currentUserUidProvider.requireUid()
                    } catch (error: Throwable) {
                        return@switchMap Observable.just(emptyList())
                    }
                    val patientUids = links.map { it.patientUid }.distinct()
                    val managedUids = listOf(selfUid) + patientUids
                    val distinctUids = managedUids.distinct()
                    if (distinctUids.isEmpty()) return@switchMap Observable.just(emptyList())
                    observeCaregiverPrefs()
                        .switchMap { prefs ->
                            val observers = distinctUids.map { uid ->
                                val isSelf = uid == selfUid
                                Observable.combineLatest(
                                    observePersonalDataByUid(uid)
                                        .onErrorReturnItem(fallbackPersonalData()),
                                    observeMeasurementReadState(uid)
                                        .onErrorReturnItem(MeasurementReadState(uid, emptySet())),
                                    observeRecentMeasurementsByUid(uid, MEASUREMENTS_LIMIT)
                                        .onErrorReturnItem(emptyList()),
                                    observeMedicationReminderEvents(uid, REMINDER_LIMIT)
                                        .onErrorReturnItem(emptyList()),
                                    observeMedicationReminderReadState(uid)
                                        .onErrorReturnItem(MedicationReminderReadState(uid, emptySet()))
                                ) { data, readState, measurements, reminders, reminderReadState ->
                                    mapItem(
                                        uid,
                                        data as PersonalData,
                                        readState as MeasurementReadState,
                                        measurements as List<Measurement>,
                                        reminders as List<MedicationReminderEvent>,
                                        reminderReadState as MedicationReminderReadState,
                                        isSelf,
                                        prefs[uid] ?: true
                                    )
                                }.onErrorReturnItem(fallbackItem(uid, isSelf))
                            }
                            Observable.combineLatest(observers) { items ->
                                items.map { it as CaregiverDependentTileUiModel }
                            }
                        }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ items ->
                    _dependents.value = items
                }, {
                    _dependents.value = emptyList()
                })
        )
    }

    fun selectDependent(uid: String) {
        disposables.add(
            setActivePatient(uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _navTarget.value = uid
                }, {
                    _navTarget.value = uid
                })
        )
    }

    fun onNavigationHandled() {
        _navTarget.value = null
    }

    fun setMedicationReminderEnabled(patientUid: String, enabled: Boolean) {
        disposables.add(
            setCaregiverPref(patientUid, enabled)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }

    fun generatePatientLinkCode(patientUid: String) {
        val cleanedUid = patientUid.trim()
        if (cleanedUid.isBlank()) {
            _message.value = "Brak podopiecznego do powiazania"
            return
        }
        val ttlSeconds = 30L * 24 * 60 * 60
        disposables.add(
            generateCareLinkCode(
                type = CareLinkCodeType.CAREGIVER_TO_PATIENT,
                ttlSeconds = ttlSeconds,
                patientUid = cleanedUid
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ code ->
                    _linkCodeToCopy.value = code.code
                }, { error ->
                    _message.value = error.message ?: "Blad generowania kodu"
                })
        )
    }

    fun onLinkCodeHandled() {
        _linkCodeToCopy.value = null
    }

    fun onMessageHandled() {
        _message.value = null
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    private fun mapItem(
        uid: String,
        data: PersonalData,
        readState: MeasurementReadState,
        measurements: List<Measurement>,
        reminders: List<MedicationReminderEvent>,
        reminderReadState: MedicationReminderReadState,
        isSelf: Boolean,
        reminderEnabled: Boolean
    ): CaregiverDependentTileUiModel {
        val firstName = data.firstName.trim()
        val lastName = data.lastName.trim()
        val fullName = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
        val phone = data.phoneNumber.trim().ifBlank { "-" }
        val readIds = readState.readMeasurementIds
        val unreadMeasurements = measurements.count { !readIds.contains(it.id) }
        val reminderReadIds = reminderReadState.readEventIds
        val unreadReminders = reminders.count { !reminderReadIds.contains(it.id) }
        val unreadCount = unreadMeasurements + unreadReminders
        return CaregiverDependentTileUiModel(
            uid = uid,
            fullName = if (fullName.isBlank()) "-" else fullName,
            phone = phone,
            avatar = initials(firstName, lastName),
            unreadCount = unreadCount,
            isSelf = isSelf,
            reminderEnabled = if (isSelf) false else reminderEnabled,
            showReminderToggle = !isSelf,
            showLinkAccountAction = !isSelf
        )
    }

    private fun fallbackItem(uid: String, isSelf: Boolean): CaregiverDependentTileUiModel {
        return CaregiverDependentTileUiModel(
            uid = uid,
            fullName = "-",
            phone = "-",
            avatar = "?",
            unreadCount = 0,
            isSelf = isSelf,
            reminderEnabled = false,
            showReminderToggle = !isSelf,
            showLinkAccountAction = !isSelf
        )
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

    private fun initials(firstName: String, lastName: String): String {
        val first = firstName.firstOrNull()?.uppercaseChar()
        val last = lastName.firstOrNull()?.uppercaseChar()
        val value = buildString {
            if (first != null) append(first)
            if (last != null) append(last)
        }
        return if (value.isBlank()) "?" else value
    }

    private companion object {
        private const val MEASUREMENTS_LIMIT = 50
        private const val REMINDER_LIMIT = 50
    }
}

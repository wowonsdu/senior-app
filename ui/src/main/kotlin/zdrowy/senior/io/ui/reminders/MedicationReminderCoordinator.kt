package zdrowy.senior.io.ui.reminders

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.carelink.CareLinkStatus
import zdrowy.senior.io.domain.carelink.ObserveCareLinksUseCase
import zdrowy.senior.io.domain.settings.Medication
import zdrowy.senior.io.domain.settings.ObserveMedicationsByUidUseCase
import zdrowy.senior.io.domain.settings.ObserveMedicationsUseCase
import zdrowy.senior.io.domain.settings.ObservePersonalDataByUidUseCase
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.domain.settings.reminders.ObserveCaregiverMedicationReminderPrefsUseCase
import zdrowy.senior.io.domain.user.CurrentUserUidProvider
import zdrowy.senior.io.domain.user.ObserveCurrentUserRoleUseCase
import zdrowy.senior.io.domain.user.UserRole
import zdrowy.senior.io.domain.user.UserRoleState

class MedicationReminderCoordinator(
    private val observeCurrentUserRole: ObserveCurrentUserRoleUseCase,
    private val observeMedications: ObserveMedicationsUseCase,
    private val observeMedicationsByUid: ObserveMedicationsByUidUseCase,
    private val observeCareLinks: ObserveCareLinksUseCase,
    private val observeCaregiverPrefs: ObserveCaregiverMedicationReminderPrefsUseCase,
    private val observePersonalDataByUid: ObservePersonalDataByUidUseCase,
    private val currentUserUidProvider: CurrentUserUidProvider,
    private val alarmScheduler: MedicationReminderAlarmScheduler
) {
    private val disposables = CompositeDisposable()
    private var started = false
    private var activeRole: ReminderRole? = null
    private var activeSpecs: Set<MedicationReminderSpec> = emptySet()

    fun start() {
        if (started) return
        started = true
        disposables.add(
            observeCurrentUserRole()
                .subscribeOn(Schedulers.io())
                .switchMap { roleState ->
                    when (roleState) {
                        is UserRoleState.Available -> {
                            if (roleState.role == UserRole.PATIENT) {
                                patientSpecsStream().map { ReminderSchedule(ReminderRole.PATIENT, it) }
                            } else {
                                caregiverSpecsStream().map { ReminderSchedule(ReminderRole.CAREGIVER, it) }
                            }
                        }
                        UserRoleState.Missing -> Observable.just(ReminderSchedule(null, emptyList()))
                    }
                }
                .subscribe({ schedule ->
                    applySchedule(schedule)
                }, {
                    applySchedule(ReminderSchedule(null, emptyList()))
                })
        )
    }

    fun stop() {
        disposables.clear()
        started = false
    }

    private fun applySchedule(schedule: ReminderSchedule) {
        val nextRole = schedule.role
        if (nextRole == null) {
            activeRole?.let { alarmScheduler.cancelAll(it) }
            activeRole = null
            activeSpecs = emptySet()
            return
        }

        if (activeRole != nextRole) {
            activeRole?.let { alarmScheduler.cancelAll(it) }
            activeRole = nextRole
            activeSpecs = emptySet()
        }

        val newSpecs = schedule.specs.toSet()
        val toCancel = activeSpecs.minus(newSpecs)
        toCancel.forEach { alarmScheduler.cancel(it) }
        alarmScheduler.scheduleAll(nextRole, newSpecs)
        activeSpecs = newSpecs
    }

    private fun patientSpecsStream(): Observable<List<MedicationReminderSpec>> {
        return observeMedications()
            .map { medications ->
                val patientUid = try {
                    currentUserUidProvider.requireUid()
                } catch (error: Throwable) {
                    return@map emptyList()
                }
                buildSpecs(ReminderRole.PATIENT, patientUid, "", medications, onlyEnabled = true)
            }
            .onErrorReturnItem(emptyList())
    }

    private fun caregiverSpecsStream(): Observable<List<MedicationReminderSpec>> {
        return observeCareLinks()
            .switchMap { links ->
                val patientUids = links
                    .filter { it.status == CareLinkStatus.ACTIVE }
                    .map { it.patientUid }
                    .distinct()
                if (patientUids.isEmpty()) return@switchMap Observable.just(emptyList())
                observeCaregiverPrefs()
                    .switchMap { prefs ->
                        val enabledUids = patientUids.filter { prefs[it] ?: true }
                        if (enabledUids.isEmpty()) return@switchMap Observable.just(emptyList())
                        val streams = enabledUids.map { uid ->
                            Observable.combineLatest(
                                observePersonalDataByUid(uid)
                                    .onErrorReturnItem(fallbackPersonalData()),
                                observeMedicationsByUid(uid)
                                    .onErrorReturnItem(emptyList())
                            ) { personal, medications ->
                                val name = mapPatientName(personal as PersonalData)
                                buildSpecs(ReminderRole.CAREGIVER, uid, name, medications as List<Medication>, false)
                            }
                        }
                        Observable.combineLatest(streams) { items ->
                            items.flatMap { it as List<MedicationReminderSpec> }
                        }
                    }
            }
            .onErrorReturnItem(emptyList())
    }

    private fun buildSpecs(
        role: ReminderRole,
        patientUid: String,
        patientName: String,
        medications: List<Medication>,
        onlyEnabled: Boolean
    ): List<MedicationReminderSpec> {
        val filtered = if (onlyEnabled) {
            medications.filter { it.notificationsEnabled }
        } else {
            medications
        }
        return filtered.flatMap { med ->
            MedicationReminderTimeUtils.parseTimes(med.schedule).map { time ->
                MedicationReminderSpec(
                    role = role,
                    patientUid = patientUid,
                    patientName = patientName,
                    medicationId = med.id,
                    medicationName = med.name,
                    dosage = med.dosage,
                    scheduleTime = time
                )
            }
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

    private data class ReminderSchedule(
        val role: ReminderRole?,
        val specs: List<MedicationReminderSpec>
    )
}

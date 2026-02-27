package zdrowy.senior.io.ui.reminders

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.carelink.CareLinkStatus
import zdrowy.senior.io.domain.carelink.ObserveCareLinksUseCase
import zdrowy.senior.io.domain.settings.ObservePersonalDataByUidUseCase
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.domain.settings.reminders.MedicationReminderEvent
import zdrowy.senior.io.domain.settings.reminders.ObserveMedicationReminderEventsUseCase
import zdrowy.senior.io.domain.user.ObserveCurrentUserRoleUseCase
import zdrowy.senior.io.domain.user.UserRole
import zdrowy.senior.io.domain.user.UserRoleState

class MedicationTakenConfirmationCoordinator(
    private val observeCurrentUserRole: ObserveCurrentUserRoleUseCase,
    private val observeCareLinks: ObserveCareLinksUseCase,
    private val observeMedicationReminderEvents: ObserveMedicationReminderEventsUseCase,
    private val observePersonalDataByUid: ObservePersonalDataByUidUseCase,
    private val notifier: MedicationReminderNotifier
) {
    private val disposables = CompositeDisposable()
    private val initializedPatients = mutableSetOf<String>()
    private val seenConfirmedByPatient = mutableMapOf<String, MutableSet<String>>()
    private var started = false

    fun start() {
        if (started) return
        started = true
        disposables.add(
            observeCurrentUserRole()
                .subscribeOn(Schedulers.io())
                .switchMap { roleState ->
                    if (roleState is UserRoleState.Available && roleState.role == UserRole.CAREGIVER) {
                        caregiverEventsStream()
                    } else {
                        Observable.just(emptyList())
                    }
                }
                .subscribe({ patients ->
                    handlePatientsSnapshot(patients)
                }, {
                    clearState()
                })
        )
    }

    fun stop() {
        disposables.clear()
        clearState()
        started = false
    }

    private fun caregiverEventsStream(): Observable<List<PatientReminderEvents>> {
        return observeCareLinks()
            .switchMap { links ->
                val patientUids = links
                    .filter { it.status == CareLinkStatus.ACTIVE }
                    .map { it.patientUid }
                    .distinct()
                if (patientUids.isEmpty()) {
                    return@switchMap Observable.just(emptyList())
                }
                val streams = patientUids.map { patientUid ->
                    Observable.combineLatest(
                        observeMedicationReminderEvents(patientUid, REMINDER_LIMIT)
                            .onErrorReturnItem(emptyList()),
                        observePersonalDataByUid(patientUid)
                            .onErrorReturnItem(fallbackPersonalData())
                    ) { events, personal ->
                        PatientReminderEvents(
                            patientUid = patientUid,
                            patientName = mapPatientName(personal as PersonalData),
                            events = events as List<MedicationReminderEvent>
                        )
                    }
                }
                Observable.combineLatest(streams) { items ->
                    items.map { it as PatientReminderEvents }
                }
            }
            .onErrorReturnItem(emptyList())
    }

    private fun handlePatientsSnapshot(patients: List<PatientReminderEvents>) {
        val activeUids = patients.map { it.patientUid }.toSet()
        initializedPatients.retainAll(activeUids)
        seenConfirmedByPatient.keys.retainAll(activeUids)

        patients.forEach { patient ->
            val confirmed = patient.events
                .filter { it.isTaken }
                .map { it.id }
                .filter { it.isNotBlank() }
                .toSet()

            val seen = seenConfirmedByPatient.getOrPut(patient.patientUid) { mutableSetOf() }
            if (!initializedPatients.contains(patient.patientUid)) {
                seen.addAll(confirmed)
                initializedPatients.add(patient.patientUid)
                return@forEach
            }

            val newlyConfirmed = confirmed.minus(seen)
            if (newlyConfirmed.isNotEmpty()) {
                patient.events
                    .filter { newlyConfirmed.contains(it.id) }
                    .forEach { event ->
                        notifier.showCaregiverTakenConfirmation(
                            eventId = event.id,
                            patientName = patient.patientName,
                            medicationName = event.medicationName,
                            dosage = event.dosage,
                            takenAtMs = event.takenAtMs
                        )
                    }
                seen.addAll(newlyConfirmed)
            }
        }
    }

    private fun clearState() {
        initializedPatients.clear()
        seenConfirmedByPatient.clear()
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

    private data class PatientReminderEvents(
        val patientUid: String,
        val patientName: String,
        val events: List<MedicationReminderEvent>
    )

    private companion object {
        private const val REMINDER_LIMIT = 50
    }
}

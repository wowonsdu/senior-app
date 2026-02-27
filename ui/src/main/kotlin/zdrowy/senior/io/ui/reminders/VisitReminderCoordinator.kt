package zdrowy.senior.io.ui.reminders

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.carelink.CareLinkStatus
import zdrowy.senior.io.domain.carelink.ObserveCareLinksUseCase
import zdrowy.senior.io.domain.settings.ObservePersonalDataByUidUseCase
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.domain.user.ObserveCurrentUserRoleUseCase
import zdrowy.senior.io.domain.user.UserRole
import zdrowy.senior.io.domain.user.UserRoleState
import zdrowy.senior.io.domain.visit.ObserveVisitsUseCase
import zdrowy.senior.io.domain.visit.Visit

class VisitReminderCoordinator(
    private val observeCurrentUserRole: ObserveCurrentUserRoleUseCase,
    private val observeCareLinks: ObserveCareLinksUseCase,
    private val observeVisits: ObserveVisitsUseCase,
    private val observePersonalDataByUid: ObservePersonalDataByUidUseCase,
    private val scheduler: VisitReminderAlarmScheduler
) {
    private val disposables = CompositeDisposable()
    private var started = false
    private var activeSpecs: Set<VisitReminderSpec> = emptySet()

    fun start() {
        if (started) return
        started = true

        disposables.add(
            observeCurrentUserRole()
                .subscribeOn(Schedulers.io())
                .switchMap { roleState ->
                    if (roleState is UserRoleState.Available && roleState.role == UserRole.CAREGIVER) {
                        caregiverSpecsStream()
                    } else {
                        Observable.just(emptySet())
                    }
                }
                .subscribe({ specs ->
                    applySpecs(specs)
                }, {
                    applySpecs(emptySet())
                })
        )
    }

    fun stop() {
        disposables.clear()
        applySpecs(emptySet())
        started = false
    }

    private fun caregiverSpecsStream(): Observable<Set<VisitReminderSpec>> {
        return observeCareLinks()
            .switchMap { links ->
                val patientUids = links
                    .filter { it.status == CareLinkStatus.ACTIVE }
                    .map { it.patientUid }
                    .distinct()
                if (patientUids.isEmpty()) {
                    return@switchMap Observable.just(emptySet())
                }
                val namesStream = observePatientNames(patientUids)
                Observable.combineLatest(
                    namesStream,
                    observeVisits().onErrorReturnItem(emptyList())
                ) { names, visits ->
                    buildSpecs(
                        patientUids = patientUids,
                        patientNames = names as Map<String, PersonalData>,
                        visits = visits as List<Visit>
                    )
                }
            }
            .onErrorReturnItem(emptySet())
    }

    private fun observePatientNames(patientUids: List<String>): Observable<Map<String, PersonalData>> {
        if (patientUids.isEmpty()) return Observable.just(emptyMap())
        val streams = patientUids.map { uid ->
            observePersonalDataByUid(uid)
                .onErrorReturnItem(fallbackPersonalData())
                .map { uid to it }
        }
        return Observable.combineLatest(streams) { array ->
            array.map { it as Pair<String, PersonalData> }.toMap()
        }
    }

    private fun buildSpecs(
        patientUids: List<String>,
        patientNames: Map<String, PersonalData>,
        visits: List<Visit>
    ): Set<VisitReminderSpec> {
        val now = System.currentTimeMillis()
        return visits
            .filter { patientUids.contains(it.patientUid) }
            .filter { !it.isCompletedManual }
            .mapNotNull { visit ->
                val offset = visit.reminderOffsetMinutes
                if (!visit.reminderEnabled || offset == null || offset <= 0) return@mapNotNull null
                val triggerAtMs = visit.scheduledAtMs - offset * 60_000L
                if (triggerAtMs <= now) return@mapNotNull null

                VisitReminderSpec(
                    visitId = visit.id,
                    patientUid = visit.patientUid,
                    patientName = mapPatientName(patientNames[visit.patientUid] ?: fallbackPersonalData()),
                    visitTitle = visit.title,
                    location = visit.location,
                    scheduledAtMs = visit.scheduledAtMs,
                    reminderOffsetMinutes = offset
                )
            }
            .toSet()
    }

    private fun applySpecs(newSpecs: Set<VisitReminderSpec>) {
        if (newSpecs == activeSpecs) return

        val toCancel = activeSpecs.minus(newSpecs)
        toCancel.forEach { scheduler.cancel(it) }

        scheduler.scheduleAll(newSpecs)
        activeSpecs = newSpecs

        if (newSpecs.isEmpty()) {
            scheduler.cancelAll()
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
}

package zdrowy.senior.io.ui.caregiver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.carelink.CareLinkStatus
import zdrowy.senior.io.domain.carelink.ObserveCareLinksUseCase
import zdrowy.senior.io.domain.settings.ObservePersonalDataByUidUseCase
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.domain.visit.EvaluateVisitStatusUseCase
import zdrowy.senior.io.domain.visit.MarkVisitCompletedUseCase
import zdrowy.senior.io.domain.visit.ObserveVisitsUseCase
import zdrowy.senior.io.domain.visit.RemoveVisitUseCase
import zdrowy.senior.io.domain.visit.Visit
import zdrowy.senior.io.domain.visit.VisitComputedStatus
import zdrowy.senior.io.ui.caregiver.model.CaregiverVisitFilter
import zdrowy.senior.io.ui.caregiver.model.CaregiverVisitItemUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CaregiverVisitsViewModel(
    private val observeCareLinks: ObserveCareLinksUseCase,
    private val observePersonalDataByUid: ObservePersonalDataByUidUseCase,
    private val observeVisits: ObserveVisitsUseCase,
    private val evaluateVisitStatus: EvaluateVisitStatusUseCase,
    private val markVisitCompleted: MarkVisitCompletedUseCase,
    private val removeVisitUseCase: RemoveVisitUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _uiState = MutableLiveData(CaregiverVisitsUiState())
    val uiState: LiveData<CaregiverVisitsUiState> = _uiState
    private val _navTarget = MutableLiveData<CaregiverVisitsNavTarget?>()
    val navTarget: LiveData<CaregiverVisitsNavTarget?> = _navTarget
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var started = false
    private var selectedFilter: CaregiverVisitFilter = CaregiverVisitFilter.UPCOMING
    private var latestAllItems: List<CaregiverVisitItemUi> = emptyList()

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
                    val namesStream = observePatientNames(patientUids)
                    Observable.combineLatest(
                        namesStream,
                        observeVisits().onErrorReturnItem(emptyList())
                    ) { names, visits ->
                        VisitsData(
                            patientUids = patientUids,
                            patientNames = names as Map<String, PersonalData>,
                            visits = visits as List<Visit>
                        )
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data ->
                    latestAllItems = data.visits
                        .filter { data.patientUids.contains(it.patientUid) }
                        .map { visit ->
                            mapVisitItem(
                                visit = visit,
                                data = data.patientNames[visit.patientUid] ?: fallbackPersonalData()
                            )
                        }
                    publishState()
                }, {
                    latestAllItems = emptyList()
                    publishState()
                })
        )
    }

    fun setFilter(filter: CaregiverVisitFilter) {
        if (selectedFilter == filter) return
        selectedFilter = filter
        publishState()
    }

    fun onAddClicked() {
        _navTarget.value = CaregiverVisitsNavTarget.ADD
    }

    fun onEditClicked(visitId: String) {
        _navTarget.value = CaregiverVisitsNavTarget.EDIT(visitId)
    }

    fun onNavigationHandled() {
        _navTarget.value = null
    }

    fun markCompleted(visitId: String) {
        disposables.add(
            markVisitCompleted(visitId, System.currentTimeMillis())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }

    fun removeVisit(visitId: String) {
        disposables.add(
            removeVisitUseCase(visitId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    private fun publishState() {
        val filtered = when (selectedFilter) {
            CaregiverVisitFilter.UPCOMING -> latestAllItems
                .filter { !it.isCompleted }
                .sortedBy { it.scheduledAtMs }
            CaregiverVisitFilter.ALL -> latestAllItems
                .sortedByDescending { it.scheduledAtMs }
            CaregiverVisitFilter.COMPLETED -> latestAllItems
                .filter { it.isCompleted }
                .sortedByDescending { it.scheduledAtMs }
        }
        _uiState.value = CaregiverVisitsUiState(
            selectedFilter = selectedFilter,
            items = filtered,
            isEmpty = filtered.isEmpty()
        )
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

    private fun mapVisitItem(
        visit: Visit,
        data: PersonalData
    ): CaregiverVisitItemUi {
        val patientName = listOf(data.firstName.trim(), data.lastName.trim())
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "-" }
        val status = evaluateVisitStatus(visit)
        val reminderLabel = if (visit.reminderEnabled && visit.reminderOffsetMinutes != null) {
            "${visit.reminderOffsetMinutes} min przed"
        } else {
            null
        }
        return CaregiverVisitItemUi(
            id = visit.id,
            patientUid = visit.patientUid,
            patientName = patientName,
            title = visit.title,
            scheduledAtMs = visit.scheduledAtMs,
            dateLabel = dateFormat.format(Date(visit.scheduledAtMs)),
            timeLabel = timeFormat.format(Date(visit.scheduledAtMs)),
            locationLabel = visit.location,
            notes = visit.notes,
            reminderEnabled = visit.reminderEnabled,
            reminderOffsetMinutes = visit.reminderOffsetMinutes,
            reminderLabel = reminderLabel,
            isCompleted = status == VisitComputedStatus.COMPLETED
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

    private data class VisitsData(
        val patientUids: List<String>,
        val patientNames: Map<String, PersonalData>,
        val visits: List<Visit>
    )
}

data class CaregiverVisitsUiState(
    val selectedFilter: CaregiverVisitFilter = CaregiverVisitFilter.UPCOMING,
    val items: List<CaregiverVisitItemUi> = emptyList(),
    val isEmpty: Boolean = true
)

sealed class CaregiverVisitsNavTarget {
    object ADD : CaregiverVisitsNavTarget()
    data class EDIT(val visitId: String) : CaregiverVisitsNavTarget()
}

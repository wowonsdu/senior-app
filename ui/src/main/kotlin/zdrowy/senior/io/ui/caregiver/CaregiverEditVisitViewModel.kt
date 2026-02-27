package zdrowy.senior.io.ui.caregiver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.carelink.ObserveCareLinksUseCase
import zdrowy.senior.io.domain.settings.ObservePersonalDataByUidUseCase
import zdrowy.senior.io.domain.visit.ObserveVisitsUseCase
import zdrowy.senior.io.domain.visit.RemoveVisitUseCase
import zdrowy.senior.io.domain.visit.UpdateVisitUseCase
import zdrowy.senior.io.domain.visit.Visit
import zdrowy.senior.io.domain.visit.VisitUpdate
import zdrowy.senior.io.ui.caregiver.model.CaregiverVisitDependentUi

class CaregiverEditVisitViewModel(
    private val observeCareLinks: ObserveCareLinksUseCase,
    private val observePersonalDataByUid: ObservePersonalDataByUidUseCase,
    private val observeVisits: ObserveVisitsUseCase,
    private val updateVisit: UpdateVisitUseCase,
    private val removeVisit: RemoveVisitUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _dependents = MutableLiveData<List<CaregiverVisitDependentUi>>(emptyList())
    val dependents: LiveData<List<CaregiverVisitDependentUi>> = _dependents
    private val _visit = MutableLiveData<Visit?>()
    val visit: LiveData<Visit?> = _visit
    private var startedVisitId: String? = null

    fun start(visitId: String) {
        if (startedVisitId == visitId) return
        startedVisitId = visitId
        disposables.add(
            CaregiverVisitDependentsProvider.stream(
                observeCareLinks = observeCareLinks,
                observePersonalDataByUid = observePersonalDataByUid
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ items ->
                    _dependents.value = items
                }, {
                    _dependents.value = emptyList()
                })
        )
        disposables.add(
            observeVisits()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ visits ->
                    _visit.value = visits.firstOrNull { it.id == visitId }
                }, {
                    _visit.value = null
                })
        )
    }

    fun updateVisit(
        visitId: String,
        update: VisitUpdate,
        onDone: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        disposables.add(
            updateVisit(visitId, update)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onDone() }, { onError(it) })
        )
    }

    fun removeVisit(
        visitId: String,
        onDone: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        disposables.add(
            removeVisit(visitId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onDone() }, { onError(it) })
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}

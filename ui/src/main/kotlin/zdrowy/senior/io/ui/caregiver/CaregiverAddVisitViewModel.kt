package zdrowy.senior.io.ui.caregiver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.carelink.ObserveCareLinksUseCase
import zdrowy.senior.io.domain.settings.ObservePersonalDataByUidUseCase
import zdrowy.senior.io.domain.visit.AddVisitUseCase
import zdrowy.senior.io.domain.visit.VisitDraft
import zdrowy.senior.io.ui.caregiver.model.CaregiverVisitDependentUi

class CaregiverAddVisitViewModel(
    private val observeCareLinks: ObserveCareLinksUseCase,
    private val observePersonalDataByUid: ObservePersonalDataByUidUseCase,
    private val addVisit: AddVisitUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _dependents = MutableLiveData<List<CaregiverVisitDependentUi>>(emptyList())
    val dependents: LiveData<List<CaregiverVisitDependentUi>> = _dependents
    private var started = false

    fun start() {
        if (started) return
        started = true
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
    }

    fun addVisit(
        draft: VisitDraft,
        onDone: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        disposables.add(
            addVisit(draft)
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

package zdrowy.senior.io.ui.caregiver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.carelink.ObserveCareLinksUseCase
import zdrowy.senior.io.domain.settings.ObservePersonalDataByUidUseCase
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.domain.user.SetActivePatientUseCase
import zdrowy.senior.io.ui.caregiver.model.CaregiverDependentTileUiModel

class CaregiverDependentsViewModel(
    private val observeCareLinks: ObserveCareLinksUseCase,
    private val observePersonalDataByUid: ObservePersonalDataByUidUseCase,
    private val setActivePatient: SetActivePatientUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _dependents = MutableLiveData<List<CaregiverDependentTileUiModel>>()
    val dependents: LiveData<List<CaregiverDependentTileUiModel>> = _dependents
    private val _navTarget = MutableLiveData<String?>()
    val navTarget: LiveData<String?> = _navTarget
    private var started = false

    fun start() {
        if (started) return
        started = true
        disposables.add(
            observeCareLinks()
                .subscribeOn(Schedulers.io())
                .switchMap { links ->
                    val patientUids = links.map { it.patientUid }.distinct()
                    if (patientUids.isEmpty()) return@switchMap Observable.just(emptyList())
                    val observers = patientUids.map { uid ->
                        observePersonalDataByUid(uid)
                            .map { data -> mapItem(uid, data) }
                            .onErrorReturnItem(fallbackItem(uid))
                    }
                    Observable.combineLatest(observers) { items ->
                        items.map { it as CaregiverDependentTileUiModel }
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

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    private fun mapItem(uid: String, data: PersonalData): CaregiverDependentTileUiModel {
        val firstName = data.firstName.trim()
        val lastName = data.lastName.trim()
        val fullName = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
        val phone = data.phoneNumber.trim().ifBlank { "-" }
        return CaregiverDependentTileUiModel(
            uid = uid,
            fullName = if (fullName.isBlank()) "-" else fullName,
            phone = phone,
            avatar = initials(firstName, lastName)
        )
    }

    private fun fallbackItem(uid: String): CaregiverDependentTileUiModel {
        return CaregiverDependentTileUiModel(
            uid = uid,
            fullName = "-",
            phone = "-",
            avatar = "?"
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
}

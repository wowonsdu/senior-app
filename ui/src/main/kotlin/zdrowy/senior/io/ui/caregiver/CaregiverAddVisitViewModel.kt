package zdrowy.senior.io.ui.caregiver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.agent.ObserveDoctorsByUidUseCase
import zdrowy.senior.io.domain.carelink.ObserveCareLinksUseCase
import zdrowy.senior.io.domain.settings.ObservePersonalDataByUidUseCase
import zdrowy.senior.io.domain.user.SetActivePatientUseCase
import zdrowy.senior.io.domain.visit.AddVisitUseCase
import zdrowy.senior.io.domain.visit.VisitDraft
import zdrowy.senior.io.ui.caregiver.model.CaregiverVisitDependentUi
import zdrowy.senior.io.ui.caregiver.model.CaregiverVisitDoctorUi

class CaregiverAddVisitViewModel(
    private val observeCareLinks: ObserveCareLinksUseCase,
    private val observePersonalDataByUid: ObservePersonalDataByUidUseCase,
    private val observeDoctorsByUid: ObserveDoctorsByUidUseCase,
    private val addVisit: AddVisitUseCase,
    private val setActivePatient: SetActivePatientUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _dependents = MutableLiveData<List<CaregiverVisitDependentUi>>(emptyList())
    val dependents: LiveData<List<CaregiverVisitDependentUi>> = _dependents
    private val _doctors = MutableLiveData<List<CaregiverVisitDoctorUi>>(emptyList())
    val doctors: LiveData<List<CaregiverVisitDoctorUi>> = _doctors
    private var doctorsDisposable: Disposable? = null
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

    fun observeDoctorsForPatient(patientUid: String) {
        doctorsDisposable?.dispose()
        doctorsDisposable = observeDoctorsByUid(patientUid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ doctors ->
                _doctors.value = doctors.map { doctor ->
                    CaregiverVisitDoctorUi(
                        id = doctor.id,
                        fullName = doctor.fullName,
                        specialization = doctor.specialization.orEmpty(),
                        phone = doctor.phone
                    )
                }
            }, {
                _doctors.value = emptyList()
            })
    }

    fun prepareAddDoctor(
        patientUid: String,
        onReady: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        disposables.add(
            setActivePatient(patientUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onReady() }, { onError(it) })
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
        doctorsDisposable?.dispose()
        disposables.clear()
        super.onCleared()
    }
}

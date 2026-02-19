package zdrowy.senior.io.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import zdrowy.senior.io.domain.session.GetActivePatientUseCase
import zdrowy.senior.io.domain.session.GetSessionUseCase
import zdrowy.senior.io.domain.session.UserRole
import zdrowy.senior.io.domain.settings.AddDiseaseUseCase
import zdrowy.senior.io.domain.settings.AddMedicineUseCase
import zdrowy.senior.io.domain.settings.GetPersonalInfoUseCase
import zdrowy.senior.io.domain.settings.ObserveDiseasesUseCase
import zdrowy.senior.io.domain.settings.ObserveMedicinesUseCase
import zdrowy.senior.io.domain.settings.PersonalInfo
import zdrowy.senior.io.domain.settings.UpdatePersonalInfoUseCase

class SettingsViewModel : ViewModel(), KoinComponent {
    private val getSessionUseCase: GetSessionUseCase by inject()
    private val getActivePatientUseCase: GetActivePatientUseCase by inject()
    private val getPersonalInfoUseCase: GetPersonalInfoUseCase by inject()
    private val updatePersonalInfoUseCase: UpdatePersonalInfoUseCase by inject()
    private val observeMedicinesUseCase: ObserveMedicinesUseCase by inject()
    private val observeDiseasesUseCase: ObserveDiseasesUseCase by inject()
    private val addMedicineUseCase: AddMedicineUseCase by inject()
    private val addDiseaseUseCase: AddDiseaseUseCase by inject()

    private val disposables = CompositeDisposable()

    private val _medicinesText = MutableLiveData<String>()
    val medicinesText: LiveData<String> = _medicinesText

    private val _diseasesText = MutableLiveData<String>()
    val diseasesText: LiveData<String> = _diseasesText

    private val _personalInfo = MutableLiveData<PersonalInfo?>()
    val personalInfo: LiveData<PersonalInfo?> = _personalInfo

    private var patientId: String? = null

    fun start() {
        val session = getSessionUseCase.execute()
        if (session == null) {
            return
        }
        patientId = when (session.role) {
            UserRole.PATIENT -> session.uid
            UserRole.CAREGIVER -> getActivePatientUseCase.execute()
        }
        val pid = patientId ?: return

        val infoDisposable = getPersonalInfoUseCase.execute(pid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { info -> _personalInfo.value = info },
                { _personalInfo.value = null },
                { _personalInfo.value = null }
            )
        disposables.add(infoDisposable)

        val medsDisposable = observeMedicinesUseCase.execute(pid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { list ->
                    _medicinesText.value = if (list.isEmpty()) {
                        "Brak lekow"
                    } else {
                        list.joinToString(separator = "\n") { item -> "${item.name} - ${item.dosage}" }
                    }
                },
                { error -> _medicinesText.value = error.message ?: "Blad" }
            )
        disposables.add(medsDisposable)

        val disDisposable = observeDiseasesUseCase.execute(pid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { list ->
                    _diseasesText.value = if (list.isEmpty()) {
                        "Brak chorob"
                    } else {
                        list.joinToString(separator = "\n") { item -> item.name }
                    }
                },
                { error -> _diseasesText.value = error.message ?: "Blad" }
            )
        disposables.add(disDisposable)
    }

    fun savePersonalInfo(info: PersonalInfo) {
        val pid = patientId ?: return
        val disposable = updatePersonalInfoUseCase.execute(pid, info)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, {})
        disposables.add(disposable)
    }

    fun addMedicine(name: String, dosage: String) {
        val pid = patientId ?: return
        val disposable = addMedicineUseCase.execute(pid, name, dosage)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, {})
        disposables.add(disposable)
    }

    fun addDisease(name: String) {
        val pid = patientId ?: return
        val disposable = addDiseaseUseCase.execute(pid, name)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, {})
        disposables.add(disposable)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}

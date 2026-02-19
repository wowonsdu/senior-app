package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.settings.Disease
import zdrowy.senior.io.domain.settings.GetPersonalDataUseCase
import zdrowy.senior.io.domain.settings.ListDiseasesUseCase
import zdrowy.senior.io.domain.settings.ListMedicationsUseCase
import zdrowy.senior.io.domain.settings.Medication
import zdrowy.senior.io.domain.settings.PersonalData

class PatientSettingsViewModel(
    private val getPersonalData: GetPersonalDataUseCase,
    private val listDiseases: ListDiseasesUseCase,
    private val listMedications: ListMedicationsUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()

    private val _personalData = MutableLiveData<PersonalData>()
    val personalData: LiveData<PersonalData> = _personalData

    private val _diseases = MutableLiveData<List<Disease>>()
    val diseases: LiveData<List<Disease>> = _diseases

    private val _medications = MutableLiveData<List<Medication>>()
    val medications: LiveData<List<Medication>> = _medications

    fun load() {
        disposables.add(
            getPersonalData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data ->
                    _personalData.value = data
                }, {
                })
        )
        disposables.add(
            listDiseases()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ items ->
                    _diseases.value = items
                }, {
                })
        )
        disposables.add(
            listMedications()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ items ->
                    _medications.value = items
                }, {
                })
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    class Factory(
        private val getPersonalData: GetPersonalDataUseCase,
        private val listDiseases: ListDiseasesUseCase,
        private val listMedications: ListMedicationsUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PatientSettingsViewModel::class.java)) {
                return PatientSettingsViewModel(
                    getPersonalData,
                    listDiseases,
                    listMedications
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

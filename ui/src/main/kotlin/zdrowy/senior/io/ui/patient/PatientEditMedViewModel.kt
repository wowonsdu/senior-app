package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.settings.Medication
import zdrowy.senior.io.domain.settings.MedicationUpdate
import zdrowy.senior.io.domain.settings.ObserveMedicationsUseCase
import zdrowy.senior.io.domain.settings.RemoveMedicationUseCase
import zdrowy.senior.io.domain.settings.UpdateMedicationUseCase

class PatientEditMedViewModel(
    private val observeMedications: ObserveMedicationsUseCase,
    private val updateMedication: UpdateMedicationUseCase,
    private val removeMedication: RemoveMedicationUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _medication = MutableLiveData<Medication?>()
    val medication: LiveData<Medication?> = _medication
    private var startedMedicationId: String? = null

    fun start(medicationId: String) {
        if (startedMedicationId == medicationId) return
        startedMedicationId = medicationId
        disposables.add(
            observeMedications()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { list -> list.firstOrNull { it.id == medicationId } }
                .distinctUntilChanged()
                .subscribe({ med ->
                    _medication.value = med
                }, {
                    _medication.value = null
                })
        )
    }

    fun updateMedication(id: String, update: MedicationUpdate, onDone: () -> Unit) {
        disposables.add(
            updateMedication(id, update)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onDone() }, { onDone() })
        )
    }

    fun removeMedication(id: String, onDone: () -> Unit) {
        disposables.add(
            removeMedication(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onDone() }, { onDone() })
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}


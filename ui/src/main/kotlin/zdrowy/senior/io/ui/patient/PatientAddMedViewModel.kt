package zdrowy.senior.io.ui.patient

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.settings.AddMedicationUseCase
import zdrowy.senior.io.domain.settings.MedicationDraft

class PatientAddMedViewModel(
    private val addMedication: AddMedicationUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()

    fun addMedication(draft: MedicationDraft, onDone: () -> Unit) {
        disposables.add(
            addMedication(draft)
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


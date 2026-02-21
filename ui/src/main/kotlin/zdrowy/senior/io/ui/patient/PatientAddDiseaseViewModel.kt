package zdrowy.senior.io.ui.patient

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.settings.AddDiseaseUseCase
import zdrowy.senior.io.domain.settings.DiseaseDraft

class PatientAddDiseaseViewModel(
    private val addDisease: AddDiseaseUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()

    fun addDisease(draft: DiseaseDraft, onDone: () -> Unit) {
        disposables.add(
            addDisease(draft)
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


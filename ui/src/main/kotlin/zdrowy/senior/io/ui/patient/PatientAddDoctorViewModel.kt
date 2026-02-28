package zdrowy.senior.io.ui.patient

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.agent.AddDoctorUseCase
import zdrowy.senior.io.domain.agent.DoctorDraft

class PatientAddDoctorViewModel(
    private val addDoctor: AddDoctorUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()

    fun addDoctor(draft: DoctorDraft, onDone: (String?) -> Unit) {
        disposables.add(
            addDoctor(draft)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ id -> onDone(id) }, { onDone(null) })
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}

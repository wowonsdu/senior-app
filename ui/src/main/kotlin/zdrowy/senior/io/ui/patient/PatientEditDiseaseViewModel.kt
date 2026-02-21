package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.settings.Disease
import zdrowy.senior.io.domain.settings.DiseaseUpdate
import zdrowy.senior.io.domain.settings.ObserveDiseasesUseCase
import zdrowy.senior.io.domain.settings.RemoveDiseaseUseCase
import zdrowy.senior.io.domain.settings.UpdateDiseaseUseCase

class PatientEditDiseaseViewModel(
    private val observeDiseases: ObserveDiseasesUseCase,
    private val updateDisease: UpdateDiseaseUseCase,
    private val removeDisease: RemoveDiseaseUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _disease = MutableLiveData<Disease?>()
    val disease: LiveData<Disease?> = _disease
    private var startedDiseaseId: String? = null

    fun start(diseaseId: String) {
        if (startedDiseaseId == diseaseId) return
        startedDiseaseId = diseaseId
        disposables.add(
            observeDiseases()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    val next = list.firstOrNull { it.id == diseaseId }
                    if (_disease.value != next) _disease.value = next
                }, {
                    _disease.value = null
                })
        )
    }

    fun updateDisease(id: String, update: DiseaseUpdate, onDone: () -> Unit) {
        disposables.add(
            updateDisease(id, update)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onDone() }, { onDone() })
        )
    }

    fun removeDisease(id: String, onDone: () -> Unit) {
        disposables.add(
            removeDisease(id)
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


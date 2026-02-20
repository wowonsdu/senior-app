package zdrowy.senior.io.ui.patient

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.measurement.AddBloodPressureMeasurementUseCase
import zdrowy.senior.io.domain.measurement.AddMeasurementUseCase
import zdrowy.senior.io.domain.measurement.DeleteMeasurementUseCase
import zdrowy.senior.io.domain.measurement.MeasurementSource
import zdrowy.senior.io.domain.measurement.MeasurementType
import zdrowy.senior.io.domain.measurement.UpdateBloodPressureMeasurementUseCase
import zdrowy.senior.io.domain.measurement.UpdateMeasurementUseCase

class PatientMeasurementDialogViewModel(
    private val addMeasurement: AddMeasurementUseCase,
    private val addBloodPressure: AddBloodPressureMeasurementUseCase,
    private val updateMeasurement: UpdateMeasurementUseCase,
    private val updateBloodPressure: UpdateBloodPressureMeasurementUseCase,
    private val deleteMeasurement: DeleteMeasurementUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()

    fun addSimpleMeasurement(
        type: MeasurementType,
        value: Double,
        timestamp: Long,
        source: MeasurementSource,
        onDone: () -> Unit
    ) {
        disposables.add(
            addMeasurement(type, value, timestamp, source)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onDone() }, { onDone() })
        )
    }

    fun addPressureMeasurement(
        systolic: Int,
        diastolic: Int,
        timestamp: Long,
        source: MeasurementSource,
        onDone: () -> Unit
    ) {
        disposables.add(
            addBloodPressure(systolic, diastolic, timestamp, source)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onDone() }, { onDone() })
        )
    }

    fun updateSimpleMeasurement(
        id: String,
        type: MeasurementType,
        value: Double,
        timestamp: Long,
        onDone: () -> Unit
    ) {
        disposables.add(
            updateMeasurement(id, type, value, timestamp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onDone() }, { onDone() })
        )
    }

    fun updatePressureMeasurement(
        id: String,
        systolic: Int,
        diastolic: Int,
        timestamp: Long,
        onDone: () -> Unit
    ) {
        disposables.add(
            updateBloodPressure(id, systolic, diastolic, timestamp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onDone() }, { onDone() })
        )
    }

    fun deleteMeasurement(id: String, onDone: () -> Unit) {
        disposables.add(
            deleteMeasurement(id)
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

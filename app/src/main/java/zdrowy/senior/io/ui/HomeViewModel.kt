package zdrowy.senior.io.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import zdrowy.senior.io.domain.measurement.AddMeasurementUseCase
import zdrowy.senior.io.domain.measurement.MeasurementType
import zdrowy.senior.io.domain.measurement.ObserveMeasurementsUseCase
import zdrowy.senior.io.domain.session.GetActivePatientUseCase
import zdrowy.senior.io.domain.session.GetSessionUseCase
import zdrowy.senior.io.domain.session.UserRole
import zdrowy.senior.io.ui.caregiver.PatientStore

class HomeViewModel : ViewModel(), KoinComponent {
    private val getSessionUseCase: GetSessionUseCase by inject()
    private val getActivePatientUseCase: GetActivePatientUseCase by inject()
    private val addMeasurementUseCase: AddMeasurementUseCase by inject()
    private val observeMeasurementsUseCase: ObserveMeasurementsUseCase by inject()
    private val patientStore: PatientStore by inject()

    private val disposables = CompositeDisposable()

    private val _uiState = MutableLiveData(HomeUiState())
    val uiState: LiveData<HomeUiState> = _uiState

    private var currentPatientId: String? = null

    fun start() {
        val session = getSessionUseCase.execute()
        if (session == null) {
            _uiState.value = HomeUiState(errorMessage = "Brak sesji")
            return
        }

        val patientId = when (session.role) {
            UserRole.PATIENT -> session.uid
            UserRole.CAREGIVER -> getActivePatientUseCase.execute()
        }
        currentPatientId = patientId

        val activeName = if (session.role == UserRole.CAREGIVER && patientId != null) {
            patientStore.getPatients().firstOrNull { it.id == patientId }?.name
        } else {
            null
        }

        _uiState.value = HomeUiState(role = session.role, activePatientName = activeName)

        if (patientId.isNullOrBlank()) {
            _uiState.value = HomeUiState(
                role = session.role,
                activePatientName = activeName,
                errorMessage = "Brak aktywnego pacjenta"
            )
            return
        }

        val disposable = observeMeasurementsUseCase.execute(patientId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { list ->
                    val text = if (list.isEmpty()) {
                        "Brak pomiarow"
                    } else {
                        list.joinToString(separator = "\n") { item ->
                            "${item.type} - ${item.valueRaw} (${item.createdAt})"
                        }
                    }
                    _uiState.value = HomeUiState(
                        role = session.role,
                        activePatientName = activeName,
                        measurementsText = text
                    )
                },
                { error ->
                    _uiState.value = HomeUiState(
                        role = session.role,
                        activePatientName = activeName,
                        errorMessage = error.message
                    )
                }
            )
        disposables.add(disposable)
    }

    fun addMeasurement(type: MeasurementType, valueRaw: String) {
        val patientId = currentPatientId ?: return
        val disposable = addMeasurementUseCase.execute(patientId, type, valueRaw)
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


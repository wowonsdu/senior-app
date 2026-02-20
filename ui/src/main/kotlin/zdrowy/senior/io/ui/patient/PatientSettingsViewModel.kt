package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.agent.Agent
import zdrowy.senior.io.domain.agent.AgentRole
import zdrowy.senior.io.domain.agent.ListAgentsUseCase
import zdrowy.senior.io.domain.settings.GetSettingsOverviewUseCase
import zdrowy.senior.io.domain.settings.ObserveDiseasesUseCase
import zdrowy.senior.io.domain.settings.ObserveMedicationsUseCase
import zdrowy.senior.io.domain.settings.ObservePersonalDataUseCase
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.domain.settings.SettingsOverview

class PatientSettingsViewModel(
    private val getSettingsOverview: GetSettingsOverviewUseCase,
    private val listAgents: ListAgentsUseCase,
    private val observePersonalData: ObservePersonalDataUseCase,
    private val observeDiseases: ObserveDiseasesUseCase,
    private val observeMedications: ObserveMedicationsUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private var settingsObserved = false
    private var currentOverview: SettingsOverview? = null
    private var currentCaregivers = emptyList<Agent>()
    private val fallbackPersonalData = PersonalData(
        firstName = "",
        lastName = "",
        pesel = "",
        phoneNumber = "",
        email = "",
        address = ""
    )

    private val _uiState = MutableLiveData<PatientSettingsUiState>()
    val uiState: LiveData<PatientSettingsUiState> = _uiState

    fun load() {
        if (currentOverview == null) {
            disposables.add(
                io.reactivex.rxjava3.core.Single.zip(
                    getSettingsOverview(),
                    listAgents()
                ) { overview, agents ->
                    currentCaregivers = agents.filter { it.role == AgentRole.CAREGIVER }
                    currentOverview = overview
                    PatientSettingsUiState.from(overview, currentCaregivers)
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ state ->
                        _uiState.value = state
                    }, {
                    })
            )
        } else {
            disposables.add(
                listAgents()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ agents ->
                        currentCaregivers = agents.filter { it.role == AgentRole.CAREGIVER }
                        val overview = currentOverview ?: return@subscribe
                        _uiState.value = PatientSettingsUiState.from(overview, currentCaregivers)
                    }, {
                    })
            )
        }
    }

    fun startSettingsObservation() {
        if (settingsObserved) return
        settingsObserved = true
        disposables.add(
            observePersonalData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data ->
                    updateOverview { it.copy(personalData = data) }
                }, {
                })
        )
        disposables.add(
            observeDiseases()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ diseases ->
                    updateOverview { it.copy(diseases = diseases) }
                }, {
                })
        )
        disposables.add(
            observeMedications()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ medications ->
                    updateOverview { it.copy(medications = medications) }
                }, {
                })
        )
    }

    private fun updateOverview(transform: (SettingsOverview) -> SettingsOverview) {
        val base = currentOverview ?: SettingsOverview(
            personalData = fallbackPersonalData,
            diseases = emptyList(),
            medications = emptyList()
        )
        currentOverview = transform(base)
        val overview = currentOverview ?: return
        _uiState.value = PatientSettingsUiState.from(overview, currentCaregivers)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

}

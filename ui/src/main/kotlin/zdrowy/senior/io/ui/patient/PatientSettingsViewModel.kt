package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.agent.AgentRole
import zdrowy.senior.io.domain.agent.ListAgentsUseCase
import zdrowy.senior.io.domain.settings.GetSettingsOverviewUseCase
import zdrowy.senior.io.domain.settings.ObservePersonalDataUseCase

class PatientSettingsViewModel(
    private val getSettingsOverview: GetSettingsOverviewUseCase,
    private val listAgents: ListAgentsUseCase,
    private val observePersonalData: ObservePersonalDataUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private var personalDataObserved = false

    private val _uiState = MutableLiveData<PatientSettingsUiState>()
    val uiState: LiveData<PatientSettingsUiState> = _uiState

    fun load() {
        disposables.add(
            io.reactivex.rxjava3.core.Single.zip(
                getSettingsOverview(),
                listAgents()
            ) { overview, agents ->
                val caregivers = agents.filter { it.role == AgentRole.CAREGIVER }
                PatientSettingsUiState.from(overview, caregivers)
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ state ->
                    _uiState.value = state
                }, {
                })
        )
    }

    fun startPersonalDataObservation() {
        if (personalDataObserved) return
        personalDataObserved = true
        disposables.add(
            observePersonalData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data ->
                    val current = _uiState.value ?: return@subscribe
                    _uiState.value = current.copy(personalData = data)
                }, {
                })
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

}

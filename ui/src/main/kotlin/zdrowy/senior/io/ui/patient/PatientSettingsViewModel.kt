package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.agent.AgentRole
import zdrowy.senior.io.domain.agent.ListAgentsUseCase
import zdrowy.senior.io.domain.settings.GetSettingsOverviewUseCase

class PatientSettingsViewModel(
    private val getSettingsOverview: GetSettingsOverviewUseCase,
    private val listAgents: ListAgentsUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()

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

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    class Factory(
        private val getSettingsOverview: GetSettingsOverviewUseCase,
        private val listAgents: ListAgentsUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PatientSettingsViewModel::class.java)) {
                return PatientSettingsViewModel(
                    getSettingsOverview,
                    listAgents
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.agent.Agent
import zdrowy.senior.io.domain.agent.AgentRole
import zdrowy.senior.io.domain.agent.AgentUpdate
import zdrowy.senior.io.domain.agent.ObserveAgentsUseCase
import zdrowy.senior.io.domain.agent.RemoveAgentUseCase
import zdrowy.senior.io.domain.agent.UpdateAgentUseCase

class PatientEditCaregiverViewModel(
    private val observeAgents: ObserveAgentsUseCase,
    private val updateAgent: UpdateAgentUseCase,
    private val removeAgent: RemoveAgentUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _caregiver = MutableLiveData<Agent?>()
    val caregiver: LiveData<Agent?> = _caregiver
    private var startedCaregiverId: String? = null

    fun start(caregiverId: String) {
        if (startedCaregiverId == caregiverId) return
        startedCaregiverId = caregiverId
        disposables.add(
            observeAgents()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    val next = list.firstOrNull { it.id == caregiverId && it.role == AgentRole.CAREGIVER }
                    if (_caregiver.value != next) _caregiver.value = next
                }, {
                    _caregiver.value = null
                })
        )
    }

    fun updateCaregiver(caregiverId: String, update: AgentUpdate, onDone: () -> Unit) {
        disposables.add(
            updateAgent(caregiverId, update)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onDone() }, { onDone() })
        )
    }

    fun removeCaregiver(caregiverId: String, onDone: () -> Unit) {
        disposables.add(
            removeAgent(caregiverId)
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

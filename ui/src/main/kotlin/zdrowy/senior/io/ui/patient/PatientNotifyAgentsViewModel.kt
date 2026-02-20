package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.agent.AgentRole
import zdrowy.senior.io.domain.agent.ListAgentsUseCase
import zdrowy.senior.io.domain.agent.NotifyAgentsUseCase

sealed class NotifyAgentsResult {
    data object Success : NotifyAgentsResult()
    data object EmptyAgents : NotifyAgentsResult()
    data object Error : NotifyAgentsResult()
}

class PatientNotifyAgentsViewModel(
    private val listAgents: ListAgentsUseCase,
    private val notifyAgents: NotifyAgentsUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _result = MutableLiveData<NotifyAgentsResult?>()
    val result: LiveData<NotifyAgentsResult?> = _result

    fun send(message: String) {
        disposables.add(
            listAgents()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ agents ->
                    val caregivers = agents.filter { it.role == AgentRole.CAREGIVER }
                    if (caregivers.isEmpty()) {
                        _result.value = NotifyAgentsResult.EmptyAgents
                        return@subscribe
                    }
                    sendToAgents(caregivers.map { it.id }, message)
                }, {
                    _result.value = NotifyAgentsResult.Error
                })
        )
    }

    fun consumeResult() {
        _result.value = null
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    private fun sendToAgents(agentIds: List<String>, message: String) {
        disposables.add(
            notifyAgents(agentIds, message)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _result.value = NotifyAgentsResult.Success
                }, {
                    _result.value = NotifyAgentsResult.Error
                })
        )
    }
}

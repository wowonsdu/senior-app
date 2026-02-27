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

class PatientEditDoctorViewModel(
    private val observeAgents: ObserveAgentsUseCase,
    private val updateAgent: UpdateAgentUseCase,
    private val removeAgent: RemoveAgentUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _doctor = MutableLiveData<Agent?>()
    val doctor: LiveData<Agent?> = _doctor
    private var startedDoctorId: String? = null

    fun start(doctorId: String) {
        if (startedDoctorId == doctorId) return
        startedDoctorId = doctorId
        disposables.add(
            observeAgents()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    val next = list.firstOrNull { it.id == doctorId && it.role == AgentRole.DOCTOR }
                    if (_doctor.value != next) _doctor.value = next
                }, {
                    _doctor.value = null
                })
        )
    }

    fun updateDoctor(doctorId: String, update: AgentUpdate, onDone: () -> Unit) {
        disposables.add(
            updateAgent(doctorId, update)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onDone() }, { onDone() })
        )
    }

    fun removeDoctor(doctorId: String, onDone: () -> Unit) {
        disposables.add(
            removeAgent(doctorId)
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

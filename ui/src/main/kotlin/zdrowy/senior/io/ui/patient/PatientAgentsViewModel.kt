package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.agent.Agent
import zdrowy.senior.io.domain.agent.ListAgentsUseCase

class PatientAgentsViewModel(
    private val listAgents: ListAgentsUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val _agents = MutableLiveData<List<Agent>>()
    val agents: LiveData<List<Agent>> = _agents

    fun loadAgents() {
        disposables.add(
            listAgents()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ items ->
                    _agents.value = items
                }, {
                })
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

}

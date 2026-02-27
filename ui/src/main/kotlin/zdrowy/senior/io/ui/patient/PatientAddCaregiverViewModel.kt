package zdrowy.senior.io.ui.patient

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.agent.AddAgentUseCase
import zdrowy.senior.io.domain.agent.AgentDraft
import zdrowy.senior.io.domain.carelink.ConsumeCareLinkCodeUseCase
import zdrowy.senior.io.domain.carelink.EnsureCaregiverContactUseCase

class PatientAddCaregiverViewModel(
    private val addAgent: AddAgentUseCase,
    private val consumeCareLinkCode: ConsumeCareLinkCodeUseCase,
    private val ensureCaregiverContact: EnsureCaregiverContactUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()

    fun addCaregiver(draft: AgentDraft, onDone: () -> Unit) {
        disposables.add(
            addAgent(draft)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onDone() }, { onDone() })
        )
    }

    fun linkByCode(code: String, onDone: () -> Unit, onError: (String) -> Unit) {
        disposables.add(
            consumeCareLinkCode(code)
                .flatMapCompletable { link -> ensureCaregiverContact(link.caregiverUid) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onDone() }, { error ->
                    onError(error.message ?: "Blad laczenia")
                })
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}

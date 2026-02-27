package zdrowy.senior.io.ui.patient

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.agent.AddAgentUseCase
import zdrowy.senior.io.domain.agent.AgentDraft
import zdrowy.senior.io.domain.carelink.CareLinkCodeType
import zdrowy.senior.io.domain.carelink.ConsumeCareLinkCodeUseCase
import zdrowy.senior.io.domain.carelink.GenerateCareLinkCodeUseCase

class PatientAddCaregiverViewModel(
    private val addAgent: AddAgentUseCase,
    private val generateCareLinkCode: GenerateCareLinkCodeUseCase,
    private val consumeCareLinkCode: ConsumeCareLinkCodeUseCase
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

    fun generateCode(onCodeGenerated: (String) -> Unit, onError: (String) -> Unit) {
        val ttlSeconds = 30L * 24 * 60 * 60
        disposables.add(
            generateCareLinkCode(CareLinkCodeType.PATIENT_TO_CAREGIVER, ttlSeconds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ code ->
                    onCodeGenerated(code.code)
                }, { error ->
                    onError(error.message ?: "Blad generowania kodu")
                })
        )
    }

    fun linkByCode(code: String, onDone: () -> Unit, onError: (String) -> Unit) {
        disposables.add(
            consumeCareLinkCode(code)
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

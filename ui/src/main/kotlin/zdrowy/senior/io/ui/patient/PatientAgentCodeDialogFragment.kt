package zdrowy.senior.io.ui.patient

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.koin.android.ext.android.get
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.DialogPatientAgentCodeBinding
import zdrowy.senior.io.domain.agent.GenerateAccessCodeForAgentUseCase
import zdrowy.senior.io.domain.agent.ListAgentsUseCase
import zdrowy.senior.io.domain.agent.SendAccessCodeSmsUseCase
import zdrowy.senior.io.domain.agent.AgentRole

class PatientAgentCodeDialogFragment : DialogFragment() {
    private var _binding: DialogPatientAgentCodeBinding? = null
    private val binding get() = _binding!!
    private val listAgentsUseCase: ListAgentsUseCase by lazy { get() }
    private val generateAccessCodeForAgentUseCase: GenerateAccessCodeForAgentUseCase by lazy { get() }
    private val sendAccessCodeSmsUseCase: SendAccessCodeSmsUseCase by lazy { get() }
    private val disposables = CompositeDisposable()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientAgentCodeBinding.inflate(layoutInflater)
        binding.patientAgentCodeClose.setOnClickListener { dismiss() }
        binding.patientAgentCodeCopy.setOnClickListener { dismiss() }
        binding.patientAgentCodeSms.setOnClickListener { sendSms() }
        loadCode()

        return MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        disposables.clear()
        _binding = null
        super.onDestroyView()
    }

    private fun loadCode() {
        disposables.add(
            listAgentsUseCase()
                .flatMap { agents ->
                    val target = agents.firstOrNull { it.role == AgentRole.CAREGIVER }
                        ?: return@flatMap io.reactivex.rxjava3.core.Single.just(null)
                    generateAccessCodeForAgentUseCase(target.id, 3600)
                }
                .subscribe({ code ->
                    if (code != null) {
                        binding.patientAgentCodeValue.text = code.code
                    }
                }, { })
        )
    }

    private fun sendSms() {
        disposables.add(
            listAgentsUseCase()
                .flatMapCompletable { agents ->
                    val target = agents.firstOrNull { it.role == AgentRole.CAREGIVER }
                        ?: return@flatMapCompletable Completable.complete()
                    generateAccessCodeForAgentUseCase(target.id, 3600)
                        .flatMapCompletable { code -> sendAccessCodeSmsUseCase(target.id, code) }
                }
                .subscribe({ dismiss() }, { dismiss() })
        )
    }
}

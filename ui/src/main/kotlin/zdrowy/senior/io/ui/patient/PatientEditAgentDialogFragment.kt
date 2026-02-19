package zdrowy.senior.io.ui.patient

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.koin.android.ext.android.get
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.DialogPatientEditAgentBinding
import zdrowy.senior.io.domain.agent.AgentRole
import zdrowy.senior.io.domain.agent.AgentUpdate
import zdrowy.senior.io.domain.agent.ListAgentsUseCase
import zdrowy.senior.io.domain.agent.RemoveAgentUseCase
import zdrowy.senior.io.domain.agent.UpdateAgentUseCase

class PatientEditAgentDialogFragment : DialogFragment() {
    private var _binding: DialogPatientEditAgentBinding? = null
    private val binding get() = _binding!!
    private val listAgentsUseCase: ListAgentsUseCase by lazy { get() }
    private val updateAgentUseCase: UpdateAgentUseCase by lazy { get() }
    private val removeAgentUseCase: RemoveAgentUseCase by lazy { get() }
    private val disposables = CompositeDisposable()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientEditAgentBinding.inflate(layoutInflater)
        binding.patientEditAgentClose.setOnClickListener { dismiss() }
        binding.patientEditAgentDelete.setOnClickListener { deleteAgent() }
        binding.patientEditAgentSave.setOnClickListener { saveAgent() }

        return MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        disposables.clear()
        _binding = null
        super.onDestroyView()
    }

    private fun saveAgent() {
        val update = AgentUpdate(
            fullName = binding.patientEditAgentName.text?.toString()?.trim().takeUnless { it.isNullOrBlank() },
            phone = binding.patientEditAgentPhone.text?.toString()?.trim().takeUnless { it.isNullOrBlank() },
            email = binding.patientEditAgentEmail.text?.toString()?.trim().takeUnless { it.isNullOrBlank() }
        )
        disposables.add(
            listAgentsUseCase()
                .flatMapCompletable { agents ->
                    val target = agents.firstOrNull { it.role == AgentRole.CAREGIVER }
                        ?: return@flatMapCompletable Completable.complete()
                    updateAgentUseCase(target.id, update)
                }
                .subscribe({ dismiss() }, { dismiss() })
        )
    }

    private fun deleteAgent() {
        disposables.add(
            listAgentsUseCase()
                .flatMapCompletable { agents ->
                    val target = agents.firstOrNull { it.role == AgentRole.CAREGIVER }
                        ?: return@flatMapCompletable Completable.complete()
                    removeAgentUseCase(target.id)
                }
                .subscribe({ dismiss() }, { dismiss() })
        )
    }
}

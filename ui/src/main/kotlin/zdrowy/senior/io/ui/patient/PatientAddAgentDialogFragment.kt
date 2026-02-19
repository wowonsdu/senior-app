package zdrowy.senior.io.ui.patient

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.koin.android.ext.android.get
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.DialogPatientAddAgentBinding
import zdrowy.senior.io.domain.agent.AddAgentUseCase
import zdrowy.senior.io.domain.agent.AgentDraft

class PatientAddAgentDialogFragment : DialogFragment() {
    private var _binding: DialogPatientAddAgentBinding? = null
    private val binding get() = _binding!!
    private val addAgentUseCase: AddAgentUseCase by lazy { get() }
    private val disposables = CompositeDisposable()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientAddAgentBinding.inflate(layoutInflater)
        binding.patientAddAgentClose.setOnClickListener { dismiss() }
        binding.patientAddAgentCancel.setOnClickListener { dismiss() }
        binding.patientAddAgentSave.setOnClickListener { saveAgent() }

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
        val name = binding.patientAddAgentName.text?.toString()?.trim().orEmpty()
        val phone = binding.patientAddAgentPhone.text?.toString()?.trim().orEmpty()
        val email = binding.patientAddAgentEmail.text?.toString()?.trim().orEmpty()
        if (name.isBlank() || phone.isBlank() || email.isBlank()) {
            return
        }
        disposables.add(
            addAgentUseCase(AgentDraft(name, phone, email))
                .subscribe({ dismiss() }, { dismiss() })
        )
    }
}

package zdrowy.senior.io.ui.patient

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.DialogPatientNotifyAgentsBinding

class PatientNotifyAgentsDialogFragment : DialogFragment() {
    private var _binding: DialogPatientNotifyAgentsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientNotifyAgentsViewModel by viewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientNotifyAgentsBinding.inflate(layoutInflater)
        binding.patientNotifyAgentsClose.setOnClickListener { dismiss() }
        binding.patientNotifyAgentsCancel.setOnClickListener { dismiss() }
        binding.patientNotifyAgentsSend.setOnClickListener { sendNotification() }
        observeResults()

        return MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun observeResults() {
        viewModel.result.observe(this) { result ->
            when (result) {
                NotifyAgentsResult.Success -> {
                    showToast(getString(R.string.patient_notify_agents_toast_success))
                    dismiss()
                }
                NotifyAgentsResult.EmptyAgents -> {
                    showToast(getString(R.string.patient_notify_agents_toast_empty))
                }
                NotifyAgentsResult.Error -> {
                    showToast(getString(R.string.patient_notify_agents_toast_error))
                }
                null -> return@observe
            }
            viewModel.consumeResult()
        }
    }

    private fun sendNotification() {
        val message = binding.patientNotifyAgentsMessage.text?.toString()?.trim().orEmpty()
        if (!validateNotBlank(message)) return
        viewModel.send(message)
    }

    private fun validateNotBlank(value: String): Boolean {
        return if (value.isBlank()) {
            binding.patientNotifyAgentsMessageInput.error = "Pole wymagane"
            false
        } else {
            binding.patientNotifyAgentsMessageInput.error = null
            true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}

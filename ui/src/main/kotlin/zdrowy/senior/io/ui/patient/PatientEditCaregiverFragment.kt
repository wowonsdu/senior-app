package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientEditCaregiverBinding
import zdrowy.senior.io.domain.agent.AgentUpdate

class PatientEditCaregiverFragment : Fragment() {
    private var _binding: FragmentPatientEditCaregiverBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientEditCaregiverViewModel by viewModel()
    private var prefilled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientEditCaregiverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = caregiverId
        if (id == null) {
            findNavController().popBackStack()
            return
        }
        binding.patientEditCaregiverToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientEditCaregiverSave.setOnClickListener {
            saveCaregiver(id)
        }
        binding.patientEditCaregiverDelete.setOnClickListener {
            confirmDelete(id)
        }
        viewModel.start(id)
        viewModel.caregiver.observe(viewLifecycleOwner) { caregiver ->
            if (prefilled) return@observe
            if (caregiver == null) return@observe
            prefilled = true
            binding.patientEditCaregiverName.setText(caregiver.fullName)
            binding.patientEditCaregiverPhone.setText(caregiver.phone)
            binding.patientEditCaregiverEmail.setText(caregiver.email)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun saveCaregiver(id: String) {
        val name = binding.patientEditCaregiverName.text?.toString()?.trim().orEmpty()
        val phone = binding.patientEditCaregiverPhone.text?.toString()?.trim().orEmpty()
        val email = binding.patientEditCaregiverEmail.text?.toString()?.trim().orEmpty()
        if (!validateNotBlank(binding.patientEditCaregiverName, name)) return
        if (!validateNotBlank(binding.patientEditCaregiverPhone, phone)) return
        if (!validateNotBlank(binding.patientEditCaregiverEmail, email)) return

        val update = AgentUpdate(
            fullName = name.takeUnless { it.isBlank() },
            phone = phone.takeUnless { it.isBlank() },
            email = email.takeUnless { it.isBlank() }
        )
        viewModel.updateCaregiver(id, update) {
            findNavController().popBackStack()
        }
    }

    private fun confirmDelete(id: String) {
        MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setTitle(R.string.patient_edit_agent_delete)
            .setMessage(R.string.patient_edit_caregiver_delete_confirm)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.patient_edit_agent_delete) { _, _ ->
                viewModel.removeCaregiver(id) {
                    findNavController().popBackStack()
                }
            }
            .show()
    }

    private fun validateNotBlank(
        field: com.google.android.material.textfield.TextInputEditText,
        value: String
    ): Boolean {
        val layout = field.parent.parent as? com.google.android.material.textfield.TextInputLayout
        return if (value.isBlank()) {
            layout?.error = "Pole wymagane"
            false
        } else {
            layout?.error = null
            true
        }
    }

    private val caregiverId: String?
        get() = arguments?.getString("caregiverId")
}

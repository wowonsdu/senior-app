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
import zdrowy.senior.io.ui.databinding.FragmentPatientEditDoctorBinding
import zdrowy.senior.io.domain.agent.AgentUpdate

class PatientEditDoctorFragment : Fragment() {
    private var _binding: FragmentPatientEditDoctorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientEditDoctorViewModel by viewModel()
    private var prefilled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientEditDoctorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = doctorId
        if (id == null) {
            findNavController().popBackStack()
            return
        }
        binding.patientEditDoctorToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientEditDoctorSave.setOnClickListener {
            saveDoctor(id)
        }
        binding.patientEditDoctorDelete.setOnClickListener {
            confirmDelete(id)
        }

        viewModel.start(id)
        viewModel.doctor.observe(viewLifecycleOwner) { doctor ->
            if (prefilled) return@observe
            if (doctor == null) return@observe
            prefilled = true
            binding.patientEditDoctorName.setText(doctor.fullName)
            binding.patientEditDoctorPhone.setText(doctor.phone)
            binding.patientEditDoctorEmail.setText(doctor.email)
            binding.patientEditDoctorSpecialty.setText(doctor.specialization.orEmpty())
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun saveDoctor(id: String) {
        val name = binding.patientEditDoctorName.text?.toString()?.trim().orEmpty()
        val phone = binding.patientEditDoctorPhone.text?.toString()?.trim().orEmpty()
        val email = binding.patientEditDoctorEmail.text?.toString()?.trim().orEmpty()
        val specialty = binding.patientEditDoctorSpecialty.text?.toString()?.trim().orEmpty()
        if (!validateNotBlank(binding.patientEditDoctorName, name)) return
        if (!validateNotBlank(binding.patientEditDoctorPhone, phone)) return
        if (!validateNotBlank(binding.patientEditDoctorEmail, email)) return
        if (!validateNotBlank(binding.patientEditDoctorSpecialty, specialty)) return

        val update = AgentUpdate(
            fullName = name.takeUnless { it.isBlank() },
            phone = phone.takeUnless { it.isBlank() },
            email = email.takeUnless { it.isBlank() },
            specialization = specialty.takeUnless { it.isBlank() }
        )
        viewModel.updateDoctor(id, update) {
            findNavController().popBackStack()
        }
    }

    private fun confirmDelete(id: String) {
        MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setTitle(R.string.patient_edit_doctor_delete)
            .setMessage(R.string.patient_edit_doctor_delete_confirm)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.patient_edit_doctor_delete) { _, _ ->
                viewModel.removeDoctor(id) {
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

    private val doctorId: String?
        get() = arguments?.getString("doctorId")
}

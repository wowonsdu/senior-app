package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import zdrowy.senior.io.ui.databinding.FragmentPatientAddDoctorBinding
import zdrowy.senior.io.domain.agent.DoctorDraft

class PatientAddDoctorFragment : Fragment() {
    private var _binding: FragmentPatientAddDoctorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientAddDoctorViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientAddDoctorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.patientAddDoctorToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientAddDoctorCancel.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientAddDoctorSave.setOnClickListener {
            saveDoctor()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun saveDoctor() {
        val name = binding.patientAddDoctorName.text?.toString()?.trim().orEmpty()
        val phone = binding.patientAddDoctorPhone.text?.toString()?.trim().orEmpty()
        val email = binding.patientAddDoctorEmail.text?.toString()?.trim().orEmpty()
        val specialization = binding.patientAddDoctorSpecialty.text?.toString()?.trim().orEmpty()
        if (!validateNotBlank(binding.patientAddDoctorName, name)) return
        if (!validateNotBlank(binding.patientAddDoctorPhone, phone)) return
        if (!validateNotBlank(binding.patientAddDoctorEmail, email)) return
        if (!validateNotBlank(binding.patientAddDoctorSpecialty, specialization)) return
        viewModel.addDoctor(
            DoctorDraft(
                fullName = name,
                phone = phone,
                email = email,
                specialization = specialization
            )
        ) { createdDoctorId ->
            if (!createdDoctorId.isNullOrBlank()) {
                findNavController()
                    .previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(RESULT_CREATED_DOCTOR_ID, createdDoctorId)
            }
            findNavController().popBackStack()
        }
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

    companion object {
        const val RESULT_CREATED_DOCTOR_ID = "result_created_doctor_id"
    }
}

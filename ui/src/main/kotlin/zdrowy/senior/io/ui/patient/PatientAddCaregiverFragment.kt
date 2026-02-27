package zdrowy.senior.io.ui.patient

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import zdrowy.senior.io.ui.databinding.FragmentPatientAddCaregiverBinding
import zdrowy.senior.io.domain.agent.AgentDraft

class PatientAddCaregiverFragment : Fragment() {
    private var _binding: FragmentPatientAddCaregiverBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientAddCaregiverViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientAddCaregiverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.patientAddCaregiverToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientAddCaregiverCancel.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientAddCaregiverSave.setOnClickListener {
            saveCaregiver()
        }
        binding.patientAddCaregiverLink.setOnClickListener {
            linkCaregiverByCode()
        }
        binding.patientAddCaregiverGenerateCode.setOnClickListener {
            generateCode()
        }
        binding.patientAddCaregiverCopy.setOnClickListener {
            copyCode()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun saveCaregiver() {
        val name = binding.patientAddCaregiverName.text?.toString()?.trim().orEmpty()
        val phone = binding.patientAddCaregiverPhone.text?.toString()?.trim().orEmpty()
        val email = binding.patientAddCaregiverEmail.text?.toString()?.trim().orEmpty()
        if (!validateNotBlank(binding.patientAddCaregiverName, name)) return
        if (!validateNotBlank(binding.patientAddCaregiverPhone, phone)) return
        if (!validateNotBlank(binding.patientAddCaregiverEmail, email)) return
        viewModel.addCaregiver(
            AgentDraft(
                fullName = name,
                phone = phone,
                email = email
            )
        ) {
            findNavController().popBackStack()
        }
    }

    private fun linkCaregiverByCode() {
        val code = binding.patientAddCaregiverCode.text?.toString()?.trim().orEmpty()
        if (code.isBlank()) {
            binding.patientAddCaregiverCodeInput.error = "Pole wymagane"
            return
        }
        val digits = code.filter { it.isDigit() }
        if (digits.length != 6) {
            binding.patientAddCaregiverCodeInput.error = "Kod musi miec 6 cyfr"
            return
        }
        binding.patientAddCaregiverCodeInput.error = null
        viewModel.linkByCode(
            code = digits,
            onDone = {
                findNavController().popBackStack()
            },
            onError = { message ->
                binding.patientAddCaregiverCodeInput.error = message
            }
        )
    }

    private fun generateCode() {
        viewModel.generateCode(
            onCodeGenerated = { code ->
                binding.patientAddCaregiverCodeSection.visibility = View.VISIBLE
                binding.patientAddCaregiverCodeValue.text = code
                binding.patientAddCaregiverCodeInput.error = null
            },
            onError = { message ->
                binding.patientAddCaregiverCodeInput.error = message
            }
        )
    }

    private fun copyCode() {
        val code = binding.patientAddCaregiverCodeValue.text?.toString().orEmpty()
        if (code.isBlank()) return
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("careLinkCode", code))
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
}

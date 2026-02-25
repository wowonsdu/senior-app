package zdrowy.senior.io.ui.caregiver

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
import zdrowy.senior.io.domain.carelink.CareLinkDraft
import zdrowy.senior.io.ui.auth.normalizePhoneNumberPl
import zdrowy.senior.io.ui.databinding.FragmentCaregiverAddDependentBinding

class CaregiverAddDependentFragment : Fragment() {
    private var _binding: FragmentCaregiverAddDependentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CaregiverAddDependentViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaregiverAddDependentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.caregiverAddDependentToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.caregiverAddDependentCancel.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.caregiverAddDependentSave.setOnClickListener {
            generateCode()
        }
        binding.caregiverAddDependentCopy.setOnClickListener {
            copyCode()
        }

        viewModel.generatedCode.observe(viewLifecycleOwner) { code ->
            binding.caregiverAddDependentCodeSection.visibility = View.VISIBLE
            binding.caregiverAddDependentCodeValue.text = code
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            binding.caregiverAddDependentPhoneInput.error = message
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun generateCode() {
        val firstName = binding.caregiverAddDependentFirstName.text?.toString()?.trim().orEmpty()
        val lastName = binding.caregiverAddDependentLastName.text?.toString()?.trim().orEmpty()
        val pesel = binding.caregiverAddDependentPesel.text?.toString()?.trim().orEmpty()
        val phone = binding.caregiverAddDependentPhone.text?.toString()?.trim().orEmpty()
        val address = binding.caregiverAddDependentAddress.text?.toString()?.trim().orEmpty()

        if (!validateNotBlank(binding.caregiverAddDependentFirstName, firstName)) return
        if (!validateNotBlank(binding.caregiverAddDependentLastName, lastName)) return
        if (!validateNotBlank(binding.caregiverAddDependentPesel, pesel)) return
        if (!validateNotBlank(binding.caregiverAddDependentPhone, phone)) return
        val phoneE164 = normalizePhoneNumberPl(phone)
        if (phoneE164 == null) {
            binding.caregiverAddDependentPhoneInput.error = "Podaj numer w formacie +48..."
            return
        }
        binding.caregiverAddDependentPhoneInput.error = null

        val draft = CareLinkDraft(
            firstName = firstName,
            lastName = lastName,
            pesel = pesel,
            phoneNumber = phoneE164,
            address = address
        )
        viewModel.generateCode(draft)
    }

    private fun validateNotBlank(
        field: View,
        value: String
    ): Boolean {
        val layout = findTextInputLayout(field)
        return if (value.isBlank()) {
            layout?.error = "Pole wymagane"
            false
        } else {
            layout?.error = null
            true
        }
    }

    private fun findTextInputLayout(field: View): com.google.android.material.textfield.TextInputLayout? {
        val directParent = field.parent
        if (directParent is com.google.android.material.textfield.TextInputLayout) return directParent
        val parentView = directParent as? View ?: return null
        return parentView.parent as? com.google.android.material.textfield.TextInputLayout
    }

    private fun copyCode() {
        val code = binding.caregiverAddDependentCodeValue.text?.toString().orEmpty()
        if (code.isBlank()) return
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("careLinkCode", code))
    }
}

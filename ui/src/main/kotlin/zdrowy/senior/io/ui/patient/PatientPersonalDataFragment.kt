package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.ui.databinding.FragmentPatientPersonalDataBinding

class PatientPersonalDataFragment : Fragment() {
    private var _binding: FragmentPatientPersonalDataBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientPersonalDataViewModel by viewModel()
    private var prefilled = false
    private var existingEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientPersonalDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.patientPersonalToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientPersonalCancel.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientPersonalSave.setOnClickListener {
            savePersonalData()
        }

        viewModel.start()
        viewModel.personalData.observe(viewLifecycleOwner) { data ->
            if (prefilled) return@observe
            if (data == null) return@observe
            prefilled = true
            binding.patientPersonalFirstName.setText(data.firstName)
            binding.patientPersonalLastName.setText(data.lastName)
            binding.patientPersonalPesel.setText(data.pesel)
            binding.patientPersonalPhone.setText(data.phoneNumber)
            binding.patientPersonalAddress.setText(data.address)
            existingEmail = data.email
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun savePersonalData() {
        val firstName = binding.patientPersonalFirstName.text?.toString()?.trim().orEmpty()
        val lastName = binding.patientPersonalLastName.text?.toString()?.trim().orEmpty()
        val pesel = binding.patientPersonalPesel.text?.toString()?.trim().orEmpty()
        val phone = binding.patientPersonalPhone.text?.toString()?.trim().orEmpty()
        val address = binding.patientPersonalAddress.text?.toString()?.trim().orEmpty()
        if (!validateNotBlank(binding.patientPersonalFirstName, firstName)) return
        if (!validateNotBlank(binding.patientPersonalLastName, lastName)) return
        if (!validateNotBlank(binding.patientPersonalPesel, pesel)) return
        if (!validateNotBlank(binding.patientPersonalPhone, phone)) return

        val data = PersonalData(
            firstName = firstName,
            lastName = lastName,
            pesel = pesel,
            phoneNumber = phone,
            email = existingEmail,
            address = address
        )
        viewModel.upsertPersonalData(data) {
            findNavController().popBackStack()
        }
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
}


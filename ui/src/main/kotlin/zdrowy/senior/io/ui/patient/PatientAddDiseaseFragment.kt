package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import zdrowy.senior.io.domain.settings.DiseaseDraft
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientAddDiseaseBinding

class PatientAddDiseaseFragment : Fragment() {
    private var _binding: FragmentPatientAddDiseaseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientAddDiseaseViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientAddDiseaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.patientAddDiseaseToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientAddDiseaseCancel.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientAddDiseaseSave.setOnClickListener {
            saveDisease()
        }
        setupCourseDropdown()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupCourseDropdown() {
        val options = resources.getStringArray(R.array.patient_add_disease_course_options)
        val adapter = ArrayAdapter(
            requireContext(),
            com.google.android.material.R.layout.mtrl_auto_complete_simple_item,
            options
        )
        binding.patientAddDiseaseCourse.setAdapter(adapter)
        binding.patientAddDiseaseCourse.setOnClickListener { binding.patientAddDiseaseCourse.showDropDown() }
        binding.patientAddDiseaseCourse.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.patientAddDiseaseCourse.showDropDown()
        }
    }

    private fun saveDisease() {
        val name = binding.patientAddDiseaseName.text?.toString()?.trim().orEmpty()
        val severity = binding.patientAddDiseaseCourse.text?.toString()?.trim().orEmpty()
        val notes = binding.patientAddDiseaseSince.text?.toString()?.trim().orEmpty()
        if (!validateNotBlank(binding.patientAddDiseaseName, name)) return
        if (!validateNotBlank(binding.patientAddDiseaseCourse, severity)) return
        viewModel.addDisease(
            DiseaseDraft(name, severity, notes)
        ) {
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


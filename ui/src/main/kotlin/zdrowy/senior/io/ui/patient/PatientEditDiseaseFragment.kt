package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import zdrowy.senior.io.domain.settings.DiseaseUpdate
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientEditDiseaseBinding

class PatientEditDiseaseFragment : Fragment() {
    private var _binding: FragmentPatientEditDiseaseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientEditDiseaseViewModel by viewModel()
    private var prefilled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientEditDiseaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = diseaseId
        if (id == null) {
            findNavController().popBackStack()
            return
        }
        binding.patientEditDiseaseToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientEditDiseaseSave.setOnClickListener { saveDisease(id) }
        binding.patientEditDiseaseDelete.setOnClickListener { confirmDelete(id) }
        setupCourseDropdown()

        viewModel.start(id)
        viewModel.disease.observe(viewLifecycleOwner) { disease ->
            if (prefilled) return@observe
            if (disease == null) return@observe
            prefilled = true
            binding.patientEditDiseaseName.setText(disease.name)
            binding.patientEditDiseaseSince.setText(disease.notes)
            binding.patientEditDiseaseCourse.setText(disease.severity, false)
        }
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
        binding.patientEditDiseaseCourse.setAdapter(adapter)
        binding.patientEditDiseaseCourse.setOnClickListener { binding.patientEditDiseaseCourse.showDropDown() }
        binding.patientEditDiseaseCourse.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.patientEditDiseaseCourse.showDropDown()
        }
    }

    private fun saveDisease(id: String) {
        val name = binding.patientEditDiseaseName.text?.toString()?.trim().orEmpty()
        val severity = binding.patientEditDiseaseCourse.text?.toString()?.trim().orEmpty()
        val notes = binding.patientEditDiseaseSince.text?.toString()?.trim().orEmpty()
        if (!validateNotBlank(binding.patientEditDiseaseName, name)) return
        if (!validateNotBlank(binding.patientEditDiseaseCourse, severity)) return
        val update = DiseaseUpdate(
            name = name.takeUnless { it.isBlank() },
            severity = severity.takeUnless { it.isBlank() },
            notes = notes.takeUnless { it.isBlank() }
        )
        viewModel.updateDisease(id, update) {
            findNavController().popBackStack()
        }
    }

    private fun confirmDelete(id: String) {
        MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setTitle(R.string.patient_edit_disease_delete_title)
            .setMessage(R.string.patient_edit_disease_delete_message)
            .setNegativeButton(R.string.patient_add_disease_cancel, null)
            .setPositiveButton(R.string.patient_edit_remove) { _, _ ->
                viewModel.removeDisease(id) {
                    findNavController().popBackStack()
                }
            }
            .show()
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

    private val diseaseId: String?
        get() = arguments?.getString("diseaseId")
}


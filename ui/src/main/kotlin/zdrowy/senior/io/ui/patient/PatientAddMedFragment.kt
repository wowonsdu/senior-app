package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import org.koin.androidx.viewmodel.ext.android.viewModel
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientAddMedBinding
import zdrowy.senior.io.ui.databinding.ItemPatientMedTimeInputBinding
import zdrowy.senior.io.domain.settings.MedicationDraft

class PatientAddMedFragment : Fragment() {
    private var _binding: FragmentPatientAddMedBinding? = null
    private val binding get() = _binding!!
    private val timeInputs = mutableListOf<TextInputEditText>()
    private val viewModel: PatientAddMedViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientAddMedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.patientAddMedToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientAddMedCancel.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientAddMedSave.setOnClickListener {
            saveMedication()
        }
        setupFrequencyDropdown()
        renderTimeInputs(0)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupFrequencyDropdown() {
        val options = resources.getStringArray(R.array.patient_add_med_frequency_options)
        val adapter = ArrayAdapter(
            requireContext(),
            com.google.android.material.R.layout.mtrl_auto_complete_simple_item,
            options
        )
        binding.patientAddMedFrequency.setAdapter(adapter)
        binding.patientAddMedFrequency.setOnItemClickListener { _, _, _, _ ->
            renderTimeInputs(getFrequencyCount())
        }
        binding.patientAddMedFrequency.setOnClickListener { binding.patientAddMedFrequency.showDropDown() }
        binding.patientAddMedFrequency.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.patientAddMedFrequency.showDropDown()
        }
    }

    private fun getFrequencyCount(): Int {
        val text = binding.patientAddMedFrequency.text?.toString()?.trim().orEmpty()
        return text.toIntOrNull()?.coerceIn(0, 10) ?: 0
    }

    private fun renderTimeInputs(count: Int) {
        val normalized = count.coerceIn(0, 10)
        val previous = timeInputs.map { it.text?.toString().orEmpty() }
        binding.patientAddMedTimesContainer.removeAllViews()
        timeInputs.clear()
        if (normalized <= 0) {
            binding.patientAddMedTimesContainer.visibility = View.GONE
            return
        }
        binding.patientAddMedTimesContainer.visibility = View.VISIBLE
        repeat(normalized) { index ->
            val itemBinding = ItemPatientMedTimeInputBinding.inflate(
                layoutInflater,
                binding.patientAddMedTimesContainer,
                false
            )
            itemBinding.medTimeInputLayout.hint =
                getString(R.string.patient_add_med_time_hint_format, index + 1)
            itemBinding.medTimeInput.setText(previous.getOrNull(index).orEmpty())
            binding.patientAddMedTimesContainer.addView(itemBinding.root)
            timeInputs += itemBinding.medTimeInput
        }
    }

    private fun saveMedication() {
        val name = binding.patientAddMedName.text?.toString()?.trim().orEmpty()
        val dosage = binding.patientAddMedDose.text?.toString()?.trim().orEmpty()
        val frequencyText = binding.patientAddMedFrequency.text?.toString()?.trim().orEmpty()
        val frequencyCount = frequencyText.toIntOrNull()?.coerceIn(0, 10) ?: 0
        val times = timeInputs.map { it.text?.toString()?.trim().orEmpty() }
        val schedule = if (frequencyCount > 0) times.joinToString(", ") else ""
        val notificationsEnabled = binding.patientAddMedNotify.isChecked

        if (!validateNotBlank(binding.patientAddMedName, name)) return
        if (!validateNotBlank(binding.patientAddMedDose, dosage)) return
        if (!validateNotBlank(binding.patientAddMedFrequency, frequencyText)) return
        if (frequencyCount > 0 && !validateTimes(times)) return

        viewModel.addMedication(
            MedicationDraft(name, dosage, schedule, notificationsEnabled)
        ) {
            findNavController().popBackStack()
        }
    }

    private fun validateTimes(times: List<String>): Boolean {
        var valid = true
        timeInputs.forEachIndexed { index, field ->
            val value = times.getOrNull(index).orEmpty()
            if (!validateNotBlank(field, value)) valid = false
        }
        return valid
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

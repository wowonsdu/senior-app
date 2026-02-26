package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import org.koin.androidx.viewmodel.ext.android.viewModel
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientEditMedBinding
import zdrowy.senior.io.ui.databinding.ItemPatientMedTimeInputBinding
import zdrowy.senior.io.domain.settings.MedicationUpdate

class PatientEditMedFragment : Fragment() {
    private var _binding: FragmentPatientEditMedBinding? = null
    private val binding get() = _binding!!
    private val timeInputs = mutableListOf<TextInputEditText>()
    private val viewModel: PatientEditMedViewModel by viewModel()
    private var prefilled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientEditMedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = medicationId
        if (id == null) {
            findNavController().popBackStack()
            return
        }
        binding.patientEditMedToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientEditMedSave.setOnClickListener {
            saveMedication(id)
        }
        binding.patientEditMedDelete.setOnClickListener {
            confirmDelete(id)
        }
        setupFrequencyDropdown()
        renderTimeInputs(0)

        viewModel.start(id)
        viewModel.medication.observe(viewLifecycleOwner) { medication ->
            if (prefilled) return@observe
            if (medication == null) return@observe
            prefilled = true
            binding.patientEditMedName.setText(medication.name)
            binding.patientEditMedDose.setText(medication.dosage)
            applySchedule(medication.schedule)
            binding.patientEditMedNotify.isChecked = medication.notificationsEnabled
        }
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
        binding.patientEditMedFrequency.setAdapter(adapter)
        binding.patientEditMedFrequency.setOnItemClickListener { _, _, _, _ ->
            renderTimeInputs(getFrequencyCount())
        }
        binding.patientEditMedFrequency.setOnClickListener { binding.patientEditMedFrequency.showDropDown() }
        binding.patientEditMedFrequency.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.patientEditMedFrequency.showDropDown()
        }
    }

    private fun getFrequencyCount(): Int {
        val text = binding.patientEditMedFrequency.text?.toString()?.trim().orEmpty()
        return text.toIntOrNull()?.coerceIn(0, 10) ?: 0
    }

    private fun renderTimeInputs(count: Int) {
        val normalized = count.coerceIn(0, 10)
        val previous = timeInputs.map { it.text?.toString().orEmpty() }
        binding.patientEditMedTimesContainer.removeAllViews()
        timeInputs.clear()
        if (normalized <= 0) {
            binding.patientEditMedTimesContainer.visibility = View.GONE
            return
        }
        binding.patientEditMedTimesContainer.visibility = View.VISIBLE
        repeat(normalized) { index ->
            val itemBinding = ItemPatientMedTimeInputBinding.inflate(
                layoutInflater,
                binding.patientEditMedTimesContainer,
                false
            )
            itemBinding.medTimeInputLayout.hint =
                getString(R.string.patient_add_med_time_hint_format, index + 1)
            itemBinding.medTimeInput.setText(previous.getOrNull(index).orEmpty())
            TimeInputMasker.attach(itemBinding.medTimeInput)
            binding.patientEditMedTimesContainer.addView(itemBinding.root)
            timeInputs += itemBinding.medTimeInput
        }
    }

    private fun applySchedule(schedule: String) {
        val parts = schedule.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        val times = parts.filter { it.contains(":") }
        val frequency = when {
            times.isNotEmpty() -> times.size
            parts.size == 1 -> parts[0].toIntOrNull() ?: 0
            else -> 0
        }.coerceIn(0, 10)
        setFrequencyValue(frequency)
        times.forEachIndexed { index, value ->
            timeInputs.getOrNull(index)?.setText(value)
        }
    }

    private fun setFrequencyValue(count: Int) {
        binding.patientEditMedFrequency.setText(count.toString(), false)
        renderTimeInputs(count)
    }

    private fun saveMedication(id: String) {
        val name = binding.patientEditMedName.text?.toString()?.trim().orEmpty()
        val dosage = binding.patientEditMedDose.text?.toString()?.trim().orEmpty()
        val frequencyText = binding.patientEditMedFrequency.text?.toString()?.trim().orEmpty()
        val frequencyCount = frequencyText.toIntOrNull()?.coerceIn(0, 10) ?: 0
        val times = timeInputs.map { it.text?.toString()?.trim().orEmpty() }
        val normalizedTimes = times.mapNotNull { TimeInputMasker.normalize(it) }
        val schedule = if (frequencyCount > 0) normalizedTimes.joinToString(", ") else ""
        val notificationsEnabled = binding.patientEditMedNotify.isChecked

        if (!validateNotBlank(binding.patientEditMedName, name)) return
        if (!validateNotBlank(binding.patientEditMedDose, dosage)) return
        if (!validateNotBlank(binding.patientEditMedFrequency, frequencyText)) return
        if (frequencyCount > 0 && !validateTimes(times)) return

        val update = MedicationUpdate(
            name = name.takeUnless { it.isBlank() },
            dosage = dosage.takeUnless { it.isBlank() },
            schedule = schedule,
            notificationsEnabled = notificationsEnabled
        )
        viewModel.updateMedication(id, update) {
            findNavController().popBackStack()
        }
    }

    private fun confirmDelete(id: String) {
        MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setTitle(R.string.patient_edit_med_delete_title)
            .setMessage(R.string.patient_edit_med_delete_message)
            .setNegativeButton(R.string.patient_add_med_cancel, null)
            .setPositiveButton(R.string.patient_edit_remove) { _, _ ->
                viewModel.removeMedication(id) {
                    findNavController().popBackStack()
                }
            }
            .show()
    }

    private fun validateTimes(times: List<String>): Boolean {
        var valid = true
        timeInputs.forEachIndexed { index, field ->
            val value = times.getOrNull(index).orEmpty()
            if (!validateNotBlank(field, value)) {
                valid = false
                return@forEachIndexed
            }
            if (TimeInputMasker.normalize(value) == null) {
                findTextInputLayout(field)?.error =
                    getString(R.string.patient_add_med_time_format_error)
                valid = false
            } else {
                findTextInputLayout(field)?.error = null
            }
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

    private val medicationId: String?
        get() = arguments?.getString("medicationId")
}

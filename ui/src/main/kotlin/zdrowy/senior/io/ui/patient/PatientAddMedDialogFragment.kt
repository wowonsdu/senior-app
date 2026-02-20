package zdrowy.senior.io.ui.patient

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.koin.android.ext.android.get
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.DialogPatientAddMedBinding
import zdrowy.senior.io.ui.databinding.ItemPatientMedTimeInputBinding
import zdrowy.senior.io.domain.settings.AddMedicationUseCase
import zdrowy.senior.io.domain.settings.MedicationDraft

class PatientAddMedDialogFragment : DialogFragment() {
    private var _binding: DialogPatientAddMedBinding? = null
    private val binding get() = _binding!!
    private val addMedicationUseCase: AddMedicationUseCase by lazy { get() }
    private val disposables = CompositeDisposable()
    private val timeInputs = mutableListOf<TextInputEditText>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientAddMedBinding.inflate(layoutInflater)
        binding.patientAddMedClose.setOnClickListener { dismiss() }
        binding.patientAddMedCancel.setOnClickListener { dismiss() }
        binding.patientAddMedSave.setOnClickListener { saveMedication() }
        setupFrequencyDropdown()
        renderTimeInputs(0)

        return MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        disposables.clear()
        _binding = null
        super.onDestroyView()
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
        disposables.add(
            addMedicationUseCase(MedicationDraft(name, dosage, schedule, notificationsEnabled))
                .subscribe({
                    parentFragmentManager.setFragmentResult(
                        SETTINGS_REFRESH_REQUEST_KEY,
                        bundleOf()
                    )
                    dismiss()
                }, { dismiss() })
        )
    }

    private fun setupFrequencyDropdown() {
        val options = resources.getStringArray(R.array.patient_add_med_frequency_options)
        val adapter = android.widget.ArrayAdapter(
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
            binding.patientAddMedTimesContainer.visibility = android.view.View.GONE
            return
        }
        binding.patientAddMedTimesContainer.visibility = android.view.View.VISIBLE
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

    private fun validateTimes(times: List<String>): Boolean {
        var valid = true
        timeInputs.forEachIndexed { index, field ->
            val value = times.getOrNull(index).orEmpty()
            if (!validateNotBlank(field, value)) valid = false
        }
        return valid
    }

    private fun validateNotBlank(
        field: android.view.View,
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

    private fun findTextInputLayout(field: android.view.View): com.google.android.material.textfield.TextInputLayout? {
        return when (val parent = field.parent) {
            is com.google.android.material.textfield.TextInputLayout -> parent
            is android.view.View -> parent.parent as? com.google.android.material.textfield.TextInputLayout
            else -> null
        }
    }
}

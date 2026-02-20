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
import zdrowy.senior.io.ui.databinding.DialogPatientEditMedBinding
import zdrowy.senior.io.ui.databinding.ItemPatientMedTimeInputBinding
import zdrowy.senior.io.domain.settings.ListMedicationsUseCase
import zdrowy.senior.io.domain.settings.MedicationUpdate
import zdrowy.senior.io.domain.settings.RemoveMedicationUseCase
import zdrowy.senior.io.domain.settings.UpdateMedicationUseCase

class PatientEditMedDialogFragment : DialogFragment() {
    private var _binding: DialogPatientEditMedBinding? = null
    private val binding get() = _binding!!
    private val listMedicationsUseCase: ListMedicationsUseCase by lazy { get() }
    private val updateMedicationUseCase: UpdateMedicationUseCase by lazy { get() }
    private val removeMedicationUseCase: RemoveMedicationUseCase by lazy { get() }
    private val disposables = CompositeDisposable()
    private val timeInputs = mutableListOf<TextInputEditText>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientEditMedBinding.inflate(layoutInflater)
        binding.patientEditMedClose.setOnClickListener { dismiss() }
        binding.patientEditMedDelete.setOnClickListener { deleteMedication() }
        binding.patientEditMedSave.setOnClickListener { saveMedication() }
        setupFrequencyDropdown()
        renderTimeInputs(0)
        prefillMedication()

        return MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        disposables.clear()
        _binding = null
        super.onDestroyView()
    }

    private fun prefillMedication() {
        val id = medicationId ?: return
        disposables.add(
            listMedicationsUseCase()
                .subscribe({ medications ->
                    val target = medications.firstOrNull { it.id == id } ?: return@subscribe
                    binding.patientEditMedName.setText(target.name)
                    binding.patientEditMedDose.setText(target.dosage)
                    applySchedule(target.schedule)
                    binding.patientEditMedNotify.isChecked = target.notificationsEnabled
                }, { })
        )
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

    private fun saveMedication() {
        val id = medicationId ?: return
        val name = binding.patientEditMedName.text?.toString()?.trim().orEmpty()
        val dosage = binding.patientEditMedDose.text?.toString()?.trim().orEmpty()
        val frequencyText = binding.patientEditMedFrequency.text?.toString()?.trim().orEmpty()
        val frequencyCount = frequencyText.toIntOrNull()?.coerceIn(0, 10) ?: 0
        val times = timeInputs.map { it.text?.toString()?.trim().orEmpty() }
        val schedule = if (frequencyCount > 0) times.joinToString(", ") else ""
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
        disposables.add(
            updateMedicationUseCase(id, update)
                .subscribe({
                    parentFragmentManager.setFragmentResult(
                        SETTINGS_REFRESH_REQUEST_KEY,
                        bundleOf()
                    )
                    dismiss()
                }, { dismiss() })
        )
    }

    private fun deleteMedication() {
        val id = medicationId ?: return
        disposables.add(
            removeMedicationUseCase(id)
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

    private fun setFrequencyValue(count: Int) {
        binding.patientEditMedFrequency.setText(count.toString(), false)
        renderTimeInputs(count)
    }

    private fun renderTimeInputs(count: Int) {
        val normalized = count.coerceIn(0, 10)
        val previous = timeInputs.map { it.text?.toString().orEmpty() }
        binding.patientEditMedTimesContainer.removeAllViews()
        timeInputs.clear()
        if (normalized <= 0) {
            binding.patientEditMedTimesContainer.visibility = android.view.View.GONE
            return
        }
        binding.patientEditMedTimesContainer.visibility = android.view.View.VISIBLE
        repeat(normalized) { index ->
            val itemBinding = ItemPatientMedTimeInputBinding.inflate(
                layoutInflater,
                binding.patientEditMedTimesContainer,
                false
            )
            itemBinding.medTimeInputLayout.hint =
                getString(R.string.patient_add_med_time_hint_format, index + 1)
            itemBinding.medTimeInput.setText(previous.getOrNull(index).orEmpty())
            binding.patientEditMedTimesContainer.addView(itemBinding.root)
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

    private val medicationId: String?
        get() = arguments?.getString("medicationId")
}

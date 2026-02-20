package zdrowy.senior.io.ui.patient

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.koin.android.ext.android.get
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.DialogPatientEditMedBinding
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientEditMedBinding.inflate(layoutInflater)
        binding.patientEditMedClose.setOnClickListener { dismiss() }
        binding.patientEditMedDelete.setOnClickListener { deleteMedication() }
        binding.patientEditMedSave.setOnClickListener { saveMedication() }
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
                    binding.patientEditMedDose.setText(target.dosage, false)
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
        if (times.isNotEmpty()) {
            binding.patientEditMedTime1.setText(times.getOrNull(0).orEmpty())
            binding.patientEditMedTime2.setText(times.getOrNull(1).orEmpty())
            binding.patientEditMedTime3.setText(times.getOrNull(2).orEmpty())
        } else {
            binding.patientEditMedFrequency.setText(schedule, false)
        }
    }

    private fun saveMedication() {
        val id = medicationId ?: return
        val name = binding.patientEditMedName.text?.toString()?.trim().orEmpty()
        val dosage = binding.patientEditMedDose.text?.toString()?.trim().orEmpty()
        val frequency = binding.patientEditMedFrequency.text?.toString()?.trim().orEmpty()
        val time1 = binding.patientEditMedTime1.text?.toString()?.trim().orEmpty()
        val time2 = binding.patientEditMedTime2.text?.toString()?.trim().orEmpty()
        val time3 = binding.patientEditMedTime3.text?.toString()?.trim().orEmpty()
        val schedule = listOf(time1, time2, time3).filter { it.isNotBlank() }
            .joinToString(", ")
            .ifBlank { frequency }
        val notificationsEnabled = binding.patientEditMedNotify.isChecked
        if (!validateNotBlank(binding.patientEditMedName, name)) return
        if (!validateNotBlank(binding.patientEditMedDose, dosage)) return
        val update = MedicationUpdate(
            name = name.takeUnless { it.isBlank() },
            dosage = dosage.takeUnless { it.isBlank() },
            schedule = schedule.takeUnless { it.isBlank() },
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

    private fun validateNotBlank(
        field: android.view.View,
        value: String
    ): Boolean {
        val layout = field.parent?.parent as? com.google.android.material.textfield.TextInputLayout
        return if (value.isBlank()) {
            layout?.error = "Pole wymagane"
            false
        } else {
            layout?.error = null
            true
        }
    }

    private val medicationId: String?
        get() = arguments?.getString("medicationId")
}

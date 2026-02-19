package zdrowy.senior.io.ui.patient

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.koin.android.ext.android.get
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.DialogPatientAddMedBinding
import zdrowy.senior.io.domain.settings.AddMedicationUseCase
import zdrowy.senior.io.domain.settings.MedicationDraft

class PatientAddMedDialogFragment : DialogFragment() {
    private var _binding: DialogPatientAddMedBinding? = null
    private val binding get() = _binding!!
    private val addMedicationUseCase: AddMedicationUseCase by lazy { get() }
    private val disposables = CompositeDisposable()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientAddMedBinding.inflate(layoutInflater)
        binding.patientAddMedClose.setOnClickListener { dismiss() }
        binding.patientAddMedCancel.setOnClickListener { dismiss() }
        binding.patientAddMedSave.setOnClickListener { saveMedication() }

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
        val frequency = binding.patientAddMedFrequency.text?.toString()?.trim().orEmpty()
        val time1 = binding.patientAddMedTime1.text?.toString()?.trim().orEmpty()
        val time2 = binding.patientAddMedTime2.text?.toString()?.trim().orEmpty()
        val time3 = binding.patientAddMedTime3.text?.toString()?.trim().orEmpty()
        val schedule = listOf(time1, time2, time3).filter { it.isNotBlank() }
            .joinToString(", ")
            .ifBlank { frequency }
        val notificationsEnabled = binding.patientAddMedNotify.isChecked
        if (name.isBlank() || dosage.isBlank()) {
            return
        }
        disposables.add(
            addMedicationUseCase(MedicationDraft(name, dosage, schedule, notificationsEnabled))
                .subscribe({ dismiss() }, { dismiss() })
        )
    }
}

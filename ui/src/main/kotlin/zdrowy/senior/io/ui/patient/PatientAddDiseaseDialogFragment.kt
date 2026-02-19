package zdrowy.senior.io.ui.patient

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.koin.android.ext.android.get
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.DialogPatientAddDiseaseBinding
import zdrowy.senior.io.domain.settings.AddDiseaseUseCase
import zdrowy.senior.io.domain.settings.DiseaseDraft

class PatientAddDiseaseDialogFragment : DialogFragment() {
    private var _binding: DialogPatientAddDiseaseBinding? = null
    private val binding get() = _binding!!
    private val addDiseaseUseCase: AddDiseaseUseCase by lazy { get() }
    private val disposables = CompositeDisposable()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientAddDiseaseBinding.inflate(layoutInflater)
        binding.patientAddDiseaseClose.setOnClickListener { dismiss() }
        binding.patientAddDiseaseCancel.setOnClickListener { dismiss() }
        binding.patientAddDiseaseSave.setOnClickListener { saveDisease() }

        return MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        disposables.clear()
        _binding = null
        super.onDestroyView()
    }

    private fun saveDisease() {
        val name = binding.patientAddDiseaseName.text?.toString()?.trim().orEmpty()
        val severity = binding.patientAddDiseaseCourse.text?.toString()?.trim().orEmpty()
        val notes = binding.patientAddDiseaseSince.text?.toString()?.trim().orEmpty()
        if (!validateNotBlank(binding.patientAddDiseaseName, name)) return
        if (!validateNotBlank(binding.patientAddDiseaseCourse, severity)) return
        disposables.add(
            addDiseaseUseCase(DiseaseDraft(name, severity, notes))
                .subscribe({ dismiss() }, { dismiss() })
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
}

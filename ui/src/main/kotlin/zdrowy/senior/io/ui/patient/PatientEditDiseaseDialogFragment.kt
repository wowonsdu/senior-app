package zdrowy.senior.io.ui.patient

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.koin.android.ext.android.get
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.DialogPatientEditDiseaseBinding
import zdrowy.senior.io.domain.settings.DiseaseUpdate
import zdrowy.senior.io.domain.settings.ListDiseasesUseCase
import zdrowy.senior.io.domain.settings.UpdateDiseaseUseCase

class PatientEditDiseaseDialogFragment : DialogFragment() {
    private var _binding: DialogPatientEditDiseaseBinding? = null
    private val binding get() = _binding!!
    private val listDiseasesUseCase: ListDiseasesUseCase by lazy { get() }
    private val updateDiseaseUseCase: UpdateDiseaseUseCase by lazy { get() }
    private val disposables = CompositeDisposable()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientEditDiseaseBinding.inflate(layoutInflater)
        binding.patientEditDiseaseClose.setOnClickListener { dismiss() }
        binding.patientEditDiseaseCancel.setOnClickListener { dismiss() }
        binding.patientEditDiseaseSave.setOnClickListener { saveDisease() }
        prefillDisease()

        return MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        disposables.clear()
        _binding = null
        super.onDestroyView()
    }

    private fun prefillDisease() {
        val id = diseaseId ?: return
        disposables.add(
            listDiseasesUseCase()
                .subscribe({ diseases ->
                    val target = diseases.firstOrNull { it.id == id } ?: return@subscribe
                    binding.patientEditDiseaseName.setText(target.name)
                    binding.patientEditDiseaseSince.setText(target.notes)
                    binding.patientEditDiseaseCourse.setText(target.severity, false)
                }, { })
        )
    }

    private fun saveDisease() {
        val id = diseaseId ?: return
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
        disposables.add(
            updateDiseaseUseCase(id, update)
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

    private val diseaseId: String?
        get() = arguments?.getString("diseaseId")
}

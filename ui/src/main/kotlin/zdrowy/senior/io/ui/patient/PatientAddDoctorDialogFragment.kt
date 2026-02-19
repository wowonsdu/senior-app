package zdrowy.senior.io.ui.patient

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.koin.android.ext.android.get
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.DialogPatientAddDoctorBinding
import zdrowy.senior.io.domain.agent.AddDoctorUseCase
import zdrowy.senior.io.domain.agent.DoctorDraft

class PatientAddDoctorDialogFragment : DialogFragment() {
    private var _binding: DialogPatientAddDoctorBinding? = null
    private val binding get() = _binding!!
    private val addDoctorUseCase: AddDoctorUseCase by lazy { get() }
    private val disposables = CompositeDisposable()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientAddDoctorBinding.inflate(layoutInflater)
        binding.patientAddDoctorClose.setOnClickListener { dismiss() }
        binding.patientAddDoctorCancel.setOnClickListener { dismiss() }
        binding.patientAddDoctorSave.setOnClickListener { saveDoctor() }

        return MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        disposables.clear()
        _binding = null
        super.onDestroyView()
    }

    private fun saveDoctor() {
        val name = binding.patientAddDoctorName.text?.toString()?.trim().orEmpty()
        val phone = binding.patientAddDoctorPhone.text?.toString()?.trim().orEmpty()
        val email = binding.patientAddDoctorEmail.text?.toString()?.trim().orEmpty()
        val specialization = binding.patientAddDoctorSpecialty.text?.toString()?.trim().orEmpty()
        if (!validateNotBlank(binding.patientAddDoctorName, name)) return
        if (!validateNotBlank(binding.patientAddDoctorPhone, phone)) return
        if (!validateNotBlank(binding.patientAddDoctorEmail, email)) return
        if (!validateNotBlank(binding.patientAddDoctorSpecialty, specialization)) return
        disposables.add(
            addDoctorUseCase(DoctorDraft(name, phone, email, specialization))
                .subscribe({ dismiss() }, { dismiss() })
        )
    }

    private fun validateNotBlank(
        field: com.google.android.material.textfield.TextInputEditText,
        value: String
    ): Boolean {
        val layout = field.parent.parent as? com.google.android.material.textfield.TextInputLayout
        return if (value.isBlank()) {
            layout?.error = "Pole wymagane"
            false
        } else {
            layout?.error = null
            true
        }
    }
}

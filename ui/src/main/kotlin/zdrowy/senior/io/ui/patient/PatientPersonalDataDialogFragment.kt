package zdrowy.senior.io.ui.patient

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.koin.android.ext.android.get
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.DialogPatientPersonalDataBinding
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.domain.settings.UpsertPersonalDataUseCase

class PatientPersonalDataDialogFragment : DialogFragment() {
    private var _binding: DialogPatientPersonalDataBinding? = null
    private val binding get() = _binding!!
    private val upsertPersonalDataUseCase: UpsertPersonalDataUseCase by lazy { get() }
    private val disposables = CompositeDisposable()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientPersonalDataBinding.inflate(layoutInflater)
        binding.patientPersonalClose.setOnClickListener { dismiss() }
        binding.patientPersonalCancel.setOnClickListener { dismiss() }
        binding.patientPersonalSave.setOnClickListener { savePersonalData() }

        return MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        disposables.clear()
        _binding = null
        super.onDestroyView()
    }

    private fun savePersonalData() {
        val firstName = binding.patientPersonalFirstName.text?.toString()?.trim().orEmpty()
        val lastName = binding.patientPersonalLastName.text?.toString()?.trim().orEmpty()
        val phone = binding.patientPersonalPhone.text?.toString()?.trim().orEmpty()
        val address = binding.patientPersonalAddress.text?.toString()?.trim().orEmpty()
        val fullName = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
        if (fullName.isBlank() || phone.isBlank()) {
            return
        }
        val data = PersonalData(
            fullName = fullName,
            phoneNumber = phone,
            email = "",
            address = address
        )
        disposables.add(
            upsertPersonalDataUseCase(data).subscribe({ dismiss() }, { dismiss() })
        )
    }
}

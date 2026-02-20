package zdrowy.senior.io.ui.patient

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.koin.android.ext.android.get
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.DialogPatientPersonalDataBinding
import zdrowy.senior.io.domain.settings.GetPersonalDataUseCase
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.domain.settings.UpsertPersonalDataUseCase

class PatientPersonalDataDialogFragment : DialogFragment() {
    private var _binding: DialogPatientPersonalDataBinding? = null
    private val binding get() = _binding!!
    private val getPersonalDataUseCase: GetPersonalDataUseCase by lazy { get() }
    private val upsertPersonalDataUseCase: UpsertPersonalDataUseCase by lazy { get() }
    private val disposables = CompositeDisposable()
    private var existingEmail: String = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientPersonalDataBinding.inflate(layoutInflater)
        binding.patientPersonalClose.setOnClickListener { dismiss() }
        binding.patientPersonalCancel.setOnClickListener { dismiss() }
        binding.patientPersonalSave.setOnClickListener { savePersonalData() }
        prefillPersonalData()

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
        val pesel = binding.patientPersonalPesel.text?.toString()?.trim().orEmpty()
        val phone = binding.patientPersonalPhone.text?.toString()?.trim().orEmpty()
        val address = binding.patientPersonalAddress.text?.toString()?.trim().orEmpty()
        if (!validateNotBlank(binding.patientPersonalFirstName, firstName)) return
        if (!validateNotBlank(binding.patientPersonalLastName, lastName)) return
        if (!validateNotBlank(binding.patientPersonalPesel, pesel)) return
        if (!validateNotBlank(binding.patientPersonalPhone, phone)) return
        val data = PersonalData(
            firstName = firstName,
            lastName = lastName,
            pesel = pesel,
            phoneNumber = phone,
            email = existingEmail,
            address = address
        )
        disposables.add(
            upsertPersonalDataUseCase(data).subscribe({ dismiss() }, { dismiss() })
        )
    }

    private fun prefillPersonalData() {
        disposables.add(
            getPersonalDataUseCase()
                .subscribe({ data ->
                    binding.patientPersonalFirstName.setText(data.firstName)
                    binding.patientPersonalLastName.setText(data.lastName)
                    binding.patientPersonalPesel.setText(data.pesel)
                    binding.patientPersonalPhone.setText(data.phoneNumber)
                    binding.patientPersonalAddress.setText(data.address)
                    existingEmail = data.email
                }, { })
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

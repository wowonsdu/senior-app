package zdrowy.senior.io.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import zdrowy.senior.io.databinding.FragmentSettingsBinding
import zdrowy.senior.io.domain.settings.PersonalInfo

class SettingsFragment : Fragment() {
    private var binding: FragmentSettingsBinding? = null
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewBinding = binding ?: return
        viewModel.start()

        viewModel.personalInfo.observe(viewLifecycleOwner) { info ->
            if (info != null) {
                viewBinding.infoFirstNameInput.setText(info.firstName)
                viewBinding.infoLastNameInput.setText(info.lastName)
                viewBinding.infoPeselInput.setText(info.pesel)
                viewBinding.infoAddressInput.setText(info.address)
                viewBinding.infoPhoneInput.setText(info.phone)
            }
        }

        viewModel.medicinesText.observe(viewLifecycleOwner) { text ->
            viewBinding.medicinesList.text = text
        }

        viewModel.diseasesText.observe(viewLifecycleOwner) { text ->
            viewBinding.diseasesList.text = text
        }

        viewBinding.savePersonalInfoButton.setOnClickListener {
            val info = PersonalInfo(
                firstName = viewBinding.infoFirstNameInput.text.toString().trim(),
                lastName = viewBinding.infoLastNameInput.text.toString().trim(),
                pesel = viewBinding.infoPeselInput.text.toString().trim(),
                address = viewBinding.infoAddressInput.text.toString().trim(),
                phone = viewBinding.infoPhoneInput.text.toString().trim()
            )
            viewModel.savePersonalInfo(info)
        }

        viewBinding.addMedicineButton.setOnClickListener {
            val name = viewBinding.medicineNameInput.text.toString().trim()
            val dosage = viewBinding.medicineDosageInput.text.toString().trim()
            if (name.isNotBlank() && dosage.isNotBlank()) {
                viewModel.addMedicine(name, dosage)
                viewBinding.medicineNameInput.text?.clear()
                viewBinding.medicineDosageInput.text?.clear()
            }
        }

        viewBinding.addDiseaseButton.setOnClickListener {
            val name = viewBinding.diseaseNameInput.text.toString().trim()
            if (name.isNotBlank()) {
                viewModel.addDisease(name)
                viewBinding.diseaseNameInput.text?.clear()
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}


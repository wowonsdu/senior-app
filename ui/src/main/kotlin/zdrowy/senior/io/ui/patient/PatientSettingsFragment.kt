package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.get
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientSettingsBinding

class PatientSettingsFragment : Fragment() {
    private var _binding: FragmentPatientSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PatientSettingsViewModel> {
        PatientSettingsViewModel.Factory(get(), get())
    }
    private val diseasesAdapter = PatientDiseasesAdapter { disease ->
        findNavController().navigate(
            R.id.action_patientSettings_to_editDisease,
            bundleOf("diseaseId" to disease.id)
        )
    }
    private val medsAdapter = PatientMedicationsAdapter { medication ->
        findNavController().navigate(
            R.id.action_patientSettings_to_editMed,
            bundleOf("medicationId" to medication.id)
        )
    }
    private val caregiversAdapter = PatientCaregiversAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientSettingsBinding.inflate(inflater, container, false)
        binding.patientSettingsToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientSettingsAddPersonal.setOnClickListener {
            findNavController().navigate(R.id.action_patientSettings_to_personalData)
        }
        binding.patientSettingsPersonal.setOnClickListener {
            findNavController().navigate(R.id.action_patientSettings_to_personalData)
        }
        binding.patientSettingsAddDisease.setOnClickListener {
            findNavController().navigate(R.id.action_patientSettings_to_addDisease)
        }
        binding.patientSettingsAddMed.setOnClickListener {
            findNavController().navigate(R.id.action_patientSettings_to_addMed)
        }
        binding.patientSettingsAlerts.setOnClickListener {
            findNavController().navigate(R.id.action_patientSettings_to_alertsOverview)
        }
        binding.patientSettingsDiseasesList.layoutManager = LinearLayoutManager(requireContext())
        binding.patientSettingsDiseasesList.adapter = diseasesAdapter
        binding.patientSettingsMedsList.layoutManager = LinearLayoutManager(requireContext())
        binding.patientSettingsMedsList.adapter = medsAdapter
        binding.patientSettingsCaregiversList.layoutManager = LinearLayoutManager(requireContext())
        binding.patientSettingsCaregiversList.adapter = caregiversAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.load()
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.patientSettingsPersonalName.text = state.personalFullName
            binding.patientSettingsPersonalPesel.text = "PESEL: ${state.personalData.pesel}"
            binding.patientSettingsPersonalAddress.text = "Adres: ${state.personalData.address}"
            binding.patientSettingsPersonalPhone.text = "Telefon: ${state.personalData.phoneNumber}"
            diseasesAdapter.submitList(state.diseases)
            binding.patientSettingsMedsListLabel.text =
                "Lista Lekow (${state.medications.size})"
            medsAdapter.submitList(state.medications)
            caregiversAdapter.submitList(state.caregivers)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

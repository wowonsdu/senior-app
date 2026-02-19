package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        PatientSettingsViewModel.Factory(get(), get(), get())
    }
    private val diseasesAdapter = PatientDiseasesAdapter()
    private val medsAdapter = PatientMedicationsAdapter()

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.load()
        viewModel.personalData.observe(viewLifecycleOwner) { data ->
            binding.patientSettingsSubtitle.text = "Profil pacjenta: ${data.fullName}"
            binding.patientSettingsPersonalSubtitle.text =
                "${data.phoneNumber} • ${data.email}"
        }
        viewModel.diseases.observe(viewLifecycleOwner) { diseases ->
            val summary = if (diseases.isEmpty()) {
                "Brak dodanych chorob."
            } else {
                val names = diseases.joinToString { it.name }
                "${diseases.size} • $names"
            }
            binding.patientSettingsDiseasesSubtitle.text = summary
            diseasesAdapter.submitList(diseases)
        }
        viewModel.medications.observe(viewLifecycleOwner) { meds ->
            val summary = if (meds.isEmpty()) {
                "Brak dodanych lekow."
            } else {
                val names = meds.joinToString { it.name }
                "${meds.size} • $names"
            }
            binding.patientSettingsMedsSubtitle.text = summary
            medsAdapter.submitList(meds)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

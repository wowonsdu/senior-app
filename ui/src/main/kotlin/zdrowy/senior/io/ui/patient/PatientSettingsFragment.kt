package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import org.koin.android.ext.android.get
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientSettingsBinding

class PatientSettingsFragment : Fragment() {
    private var _binding: FragmentPatientSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PatientSettingsViewModel> {
        PatientSettingsViewModel.Factory(get(), get(), get())
    }

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.load()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

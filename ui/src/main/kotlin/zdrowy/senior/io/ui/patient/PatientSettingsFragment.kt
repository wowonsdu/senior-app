package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientSettingsBinding

class PatientSettingsFragment : Fragment() {
    private var _binding: FragmentPatientSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientSettingsViewModel by viewModel()
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
            findNavController().navigate(R.id.action_patientSettings_to_patientAlerts)
        }
        binding.patientSettingsAgents.setOnClickListener {
            findNavController().navigate(R.id.action_patientSettings_to_patientAgents)
        }
        binding.patientSettingsDeleteAccount.setOnClickListener {
            viewModel.onDeleteAccountClicked()
        }
        binding.patientSettingsDiseasesList.layoutManager = LinearLayoutManager(requireContext())
        binding.patientSettingsDiseasesList.adapter = diseasesAdapter
        binding.patientSettingsMedsList.layoutManager = LinearLayoutManager(requireContext())
        binding.patientSettingsMedsList.adapter = medsAdapter
        binding.patientSettingsMedsListLabel.visibility = View.GONE
        binding.patientSettingsCaregiversList.layoutManager = LinearLayoutManager(requireContext())
        binding.patientSettingsCaregiversList.adapter = caregiversAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.startSettingsObservation()
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.patientSettingsPersonalName.text = state.personalFullName
            binding.patientSettingsPersonalPesel.text = "PESEL: ${state.personalData.pesel}"
            binding.patientSettingsPersonalAddress.text = "Adres: ${state.personalData.address}"
            binding.patientSettingsPersonalPhone.text = "Telefon: ${state.personalData.phoneNumber}"
            diseasesAdapter.submitList(state.diseases)
            medsAdapter.submitList(state.medications)
            caregiversAdapter.submitList(state.caregivers)
        }
        viewModel.deletePrompt.observe(viewLifecycleOwner) { prompt ->
            if (prompt == null) return@observe
            when (prompt) {
                PatientSettingsDeletePrompt.Self -> showDeleteSelfDialog()
                PatientSettingsDeletePrompt.Caregiver -> showDeleteCaregiverOptions()
            }
            viewModel.onDeletePromptHandled()
        }
        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message.isNullOrBlank()) return@observe
            showToast(message)
            viewModel.onMessageHandled()
        }
        viewModel.navTarget.observe(viewLifecycleOwner) { target ->
            if (target == null) return@observe
            when (target) {
                PatientSettingsNavTarget.STARTUP_GATE -> navigateToStartup()
                PatientSettingsNavTarget.CAREGIVER_HOME -> navigateToCaregiverHome()
            }
            viewModel.onNavigationHandled()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun showDeleteSelfDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setTitle(R.string.patient_settings_delete_account_title)
            .setMessage(R.string.patient_settings_delete_account_confirm)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.patient_settings_delete_action_confirm) { _, _ ->
                viewModel.confirmDeleteSelf()
            }
            .show()
    }

    private fun showDeletePatientDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setTitle(R.string.patient_settings_delete_account_title)
            .setMessage(R.string.patient_settings_delete_patient_confirm)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.patient_settings_delete_action_confirm) { _, _ ->
                viewModel.confirmDeletePatient()
            }
            .show()
    }

    private fun showUnlinkDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setTitle(R.string.patient_settings_delete_account_title)
            .setMessage(R.string.patient_settings_unlink_confirm)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.patient_settings_delete_action_unlink) { _, _ ->
                viewModel.confirmStopCaregiving()
            }
            .show()
    }

    private fun showDeleteCaregiverOptions() {
        val options = arrayOf(
            getString(R.string.patient_settings_delete_option_self),
            getString(R.string.patient_settings_delete_option_patient),
            getString(R.string.patient_settings_delete_option_unlink)
        )
        MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setTitle(R.string.patient_settings_delete_options_title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showDeleteSelfDialog()
                    1 -> showDeletePatientDialog()
                    2 -> showUnlinkDialog()
                }
            }
            .show()
    }

    private fun navigateToStartup() {
        findNavController().navigate(
            R.id.startupGateFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.patientHomeFragment, true)
                .setLaunchSingleTop(true)
                .build()
        )
    }

    private fun navigateToCaregiverHome() {
        val handled = findNavController().popBackStack(R.id.caregiverHomeFragment, false)
        if (!handled) {
            findNavController().popBackStack()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}

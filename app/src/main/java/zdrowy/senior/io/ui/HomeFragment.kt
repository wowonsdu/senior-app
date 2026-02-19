package zdrowy.senior.io.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import zdrowy.senior.io.R
import zdrowy.senior.io.databinding.FragmentHomeBinding
import zdrowy.senior.io.domain.measurement.MeasurementType
import zdrowy.senior.io.domain.session.ClearSessionUseCase
import zdrowy.senior.io.domain.session.SetActivePatientUseCase
import zdrowy.senior.io.domain.session.UserRole

class HomeFragment : Fragment(), KoinComponent {
    private var binding: FragmentHomeBinding? = null
    private val clearSessionUseCase: ClearSessionUseCase by inject()
    private val setActivePatientUseCase: SetActivePatientUseCase by inject()
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewBinding = binding ?: return
        viewModel.start()

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            viewBinding.activePatientText.text = state.activePatientName ?: getString(R.string.patient_none_selected)
            viewBinding.measurementsText.text = state.measurementsText
            viewBinding.errorText.visibility = if (state.errorMessage.isNullOrBlank()) View.GONE else View.VISIBLE
            viewBinding.errorText.text = state.errorMessage

            val isCaregiver = state.role == UserRole.CAREGIVER
            viewBinding.activePatientCard.visibility = if (isCaregiver) View.VISIBLE else View.GONE
            viewBinding.switchPatientButton.visibility = if (isCaregiver) View.VISIBLE else View.GONE

            val showMeasurements = !state.measurementsText.isNullOrBlank() &&
                state.measurementsText != getString(R.string.measurement_list_empty)
            viewBinding.measurementsCard.visibility = if (showMeasurements) View.VISIBLE else View.GONE
        }

        viewBinding.measureGlucoseCard.setOnClickListener {
            showMeasurementDialog(MeasurementType.GLUCOSE)
        }
        viewBinding.measureInsulinCard.setOnClickListener {
            showMeasurementDialog(MeasurementType.INSULIN)
        }
        viewBinding.measurePressureCard.setOnClickListener {
            showMeasurementDialog(MeasurementType.PRESSURE)
        }
        viewBinding.measurePulseCard.setOnClickListener {
            showMeasurementDialog(MeasurementType.PULSE)
        }

        viewBinding.historyButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_history)
        }

        viewBinding.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }

        viewBinding.contactsButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_contacts)
        }

        viewBinding.alertsButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_alerts)
        }

        viewBinding.visitsButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_visits)
        }

        viewBinding.switchPatientButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_patientSelection)
        }

        viewBinding.notifyAgentsCard.setOnClickListener {
            Toast.makeText(requireContext(), "Powiadomienie wysłane do agentów (mock)", Toast.LENGTH_SHORT).show()
        }

        viewBinding.notifyDoctorsCard.setOnClickListener {
            Toast.makeText(requireContext(), "Powiadomienie wysłane do lekarzy (mock)", Toast.LENGTH_SHORT).show()
        }

        viewBinding.emergencyButton.setOnClickListener {
            Toast.makeText(requireContext(), "Wezwano pomoc (mock)", Toast.LENGTH_SHORT).show()
        }

        viewBinding.logoutButton.setOnClickListener {
            clearSessionUseCase.execute()
            setActivePatientUseCase.execute(null)
            findNavController().navigate(R.id.action_home_to_roleSelection)
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun showMeasurementDialog(type: MeasurementType) {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_measurement_input, null, false)
        val input = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.measurement_input)
        val label = when (type) {
            MeasurementType.GLUCOSE -> getString(R.string.alert_glucose)
            MeasurementType.INSULIN -> getString(R.string.alert_insulin)
            MeasurementType.PRESSURE -> getString(R.string.alert_pressure)
            MeasurementType.PULSE -> getString(R.string.alert_pulse)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("${getString(R.string.measurement_add)}: $label")
            .setView(dialogView)
            .setPositiveButton(getString(R.string.measurement_add)) { _, _ ->
                val value = input.text?.toString()?.trim().orEmpty()
                if (value.isNotBlank()) {
                    viewModel.addMeasurement(type, value)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        const val ARG_USER_ID = "arg_user_id"
    }
}

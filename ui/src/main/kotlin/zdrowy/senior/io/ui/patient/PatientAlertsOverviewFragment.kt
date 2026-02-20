package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientAlertsOverviewBinding
import zdrowy.senior.io.domain.alert.AlertConfig
import zdrowy.senior.io.domain.measurement.MeasurementType

class PatientAlertsOverviewFragment : Fragment() {
    private var _binding: FragmentPatientAlertsOverviewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientAlertsViewModel by viewModel()
    private val adapter = PatientAlertsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientAlertsOverviewBinding.inflate(inflater, container, false)
        binding.patientAlertsOverviewToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientAlertsOverviewContent.alertsOverviewSave.setOnClickListener {
            findNavController().navigate(R.id.action_alertsOverview_to_alertsConfig)
        }
        binding.patientAlertsList.layoutManager = LinearLayoutManager(requireContext())
        binding.patientAlertsList.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.load()
        viewModel.alertConfig.observe(viewLifecycleOwner) { config ->
            bindAlertConfig(config)
            adapter.submitList(config.settings)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindAlertConfig(config: AlertConfig) {
        val content = binding.patientAlertsOverviewContent
        val byType = config.settings.associateBy { it.type }
        content.alertsOverviewItemSugar.setSubtitle(
            formatAlertSubtitle(byType[MeasurementType.SUGAR])
        )
        content.alertsOverviewItemInsulin.setSubtitle(
            formatAlertSubtitle(byType[MeasurementType.INSULIN])
        )
        content.alertsOverviewItemPressure.setSubtitle(
            formatAlertSubtitle(byType[MeasurementType.PRESSURE])
        )
        content.alertsOverviewItemPulse.setSubtitle(
            formatAlertSubtitle(byType[MeasurementType.PULSE])
        )
    }

    private fun formatAlertSubtitle(setting: zdrowy.senior.io.domain.alert.AlertSetting?): String {
        if (setting == null) return "Brak danych."
        val enabled = if (setting.enabled) "wlaczony" else "wylaczony"
        val min = setting.min?.toString() ?: "-"
        val max = setting.max?.toString() ?: "-"
        return "Status: $enabled \u2022 Min: $min \u2022 Max: $max"
    }
}

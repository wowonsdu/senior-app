package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.core.content.ContextCompat
import org.koin.android.ext.android.get
import zdrowy.senior.io.ui.databinding.FragmentPatientHistoryBinding
import zdrowy.senior.io.domain.measurement.Measurement
import zdrowy.senior.io.domain.measurement.MeasurementType
import zdrowy.senior.io.ui.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PatientHistoryFragment : Fragment() {
    private var _binding: FragmentPatientHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PatientHistoryViewModel> {
        PatientHistoryViewModel.Factory(get(), get(), get())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientHistoryBinding.inflate(inflater, container, false)
        binding.patientHistoryToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.load()
        viewModel.measurements.observe(viewLifecycleOwner) { items ->
            bindMeasurements(items)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindMeasurements(items: List<Measurement>) {
        val slots = listOf(
            binding.patientHistoryItemPrimary,
            binding.patientHistoryItemSecondary,
            binding.patientHistoryItemTertiary
        )
        val formatter = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
        slots.forEach { it.visibility = View.GONE }

        items.take(slots.size).forEachIndexed { index, measurement ->
            val view = slots[index]
            view.visibility = View.VISIBLE
            view.setTitle(formatTitle(measurement))
            view.setSubtitle(formatter.format(Date(measurement.timestamp)))
            view.setIconRes(iconFor(measurement.type))
            view.setIconTint(ContextCompat.getColor(requireContext(), tintFor(measurement.type)))
        }
    }

    private fun formatTitle(measurement: Measurement): String {
        return when (measurement.type) {
            MeasurementType.PRESSURE -> {
                val systolic = measurement.systolic ?: 0
                val diastolic = measurement.diastolic ?: 0
                "Cisnienie: ${systolic}/${diastolic} mmHg"
            }
            MeasurementType.SUGAR -> "Cukier: ${measurement.value ?: 0.0} mg/dl"
            MeasurementType.INSULIN -> "Insulina: ${measurement.value ?: 0.0} j."
            MeasurementType.PULSE -> "Tetno: ${measurement.value ?: 0.0} bpm"
        }
    }

    private fun iconFor(type: MeasurementType): Int {
        return when (type) {
            MeasurementType.SUGAR -> R.drawable.ic_alert_drop
            MeasurementType.INSULIN -> R.drawable.ic_alert_medical
            MeasurementType.PRESSURE -> R.drawable.ic_alert_heart
            MeasurementType.PULSE -> R.drawable.ic_alert_pulse
        }
    }

    private fun tintFor(type: MeasurementType): Int {
        return when (type) {
            MeasurementType.SUGAR -> R.color.senior_info
            MeasurementType.INSULIN -> R.color.senior_secondary
            MeasurementType.PRESSURE -> R.color.senior_danger
            MeasurementType.PULSE -> R.color.senior_purple
        }
    }
}

package zdrowy.senior.io.ui.alert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import zdrowy.senior.io.databinding.FragmentAlertSettingsBinding
import zdrowy.senior.io.domain.alert.AlertSetting
import zdrowy.senior.io.domain.measurement.MeasurementType

class AlertSettingsFragment : Fragment() {
    private var binding: FragmentAlertSettingsBinding? = null
    private val viewModel: AlertSettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlertSettingsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewBinding = binding ?: return
        viewModel.start()

        viewModel.settings.observe(viewLifecycleOwner) { settings ->
            val map = settings.associateBy { it.type }
            applySetting(map[MeasurementType.GLUCOSE], viewBinding.glucoseEnabled, viewBinding.glucoseLow, viewBinding.glucoseHigh)
            applySetting(map[MeasurementType.INSULIN], viewBinding.insulinEnabled, viewBinding.insulinLow, viewBinding.insulinHigh)
            applySetting(map[MeasurementType.PRESSURE], viewBinding.pressureEnabled, viewBinding.pressureLow, viewBinding.pressureHigh)
            applySetting(map[MeasurementType.PULSE], viewBinding.pulseEnabled, viewBinding.pulseLow, viewBinding.pulseHigh)
        }

        viewBinding.saveAlertSettingsButton.setOnClickListener {
            val updated = listOf(
                readSetting(MeasurementType.GLUCOSE, viewBinding.glucoseEnabled, viewBinding.glucoseLow, viewBinding.glucoseHigh),
                readSetting(MeasurementType.INSULIN, viewBinding.insulinEnabled, viewBinding.insulinLow, viewBinding.insulinHigh),
                readSetting(MeasurementType.PRESSURE, viewBinding.pressureEnabled, viewBinding.pressureLow, viewBinding.pressureHigh),
                readSetting(MeasurementType.PULSE, viewBinding.pulseEnabled, viewBinding.pulseLow, viewBinding.pulseHigh)
            )
            viewModel.saveSettings(updated)
        }
    }

    private fun applySetting(setting: AlertSetting?, enabled: CheckBox, low: EditText, high: EditText) {
        if (setting == null) return
        enabled.isChecked = setting.enabled
        low.setText(setting.lowThreshold)
        high.setText(setting.highThreshold)
    }

    private fun readSetting(
        type: MeasurementType,
        enabled: CheckBox,
        low: EditText,
        high: EditText
    ): AlertSetting {
        return AlertSetting(
            type = type,
            enabled = enabled.isChecked,
            lowThreshold = low.text.toString().trim(),
            highThreshold = high.text.toString().trim()
        )
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}


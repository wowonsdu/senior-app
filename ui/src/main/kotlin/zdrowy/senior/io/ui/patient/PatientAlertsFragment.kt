package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import zdrowy.senior.io.ui.databinding.FragmentPatientAlertsBinding
import zdrowy.senior.io.ui.R

class PatientAlertsFragment : Fragment() {
    private var _binding: FragmentPatientAlertsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientAlertsViewModel by viewModel()

    private var isSugarExpanded: Boolean = true
    private var isInsulinExpanded: Boolean = false
    private var isPressureExpanded: Boolean = false
    private var isPulseExpanded: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientAlertsBinding.inflate(inflater, container, false)
        binding.patientAlertsToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isSugarExpanded = savedInstanceState?.getBoolean(KEY_SUGAR_EXPANDED) ?: true
        isInsulinExpanded = savedInstanceState?.getBoolean(KEY_INSULIN_EXPANDED) ?: false
        isPressureExpanded = savedInstanceState?.getBoolean(KEY_PRESSURE_EXPANDED) ?: false
        isPulseExpanded = savedInstanceState?.getBoolean(KEY_PULSE_EXPANDED) ?: false

        setupChevronToggles()
        setupPressureConfigUi()
        viewModel.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_SUGAR_EXPANDED, isSugarExpanded)
        outState.putBoolean(KEY_INSULIN_EXPANDED, isInsulinExpanded)
        outState.putBoolean(KEY_PRESSURE_EXPANDED, isPressureExpanded)
        outState.putBoolean(KEY_PULSE_EXPANDED, isPulseExpanded)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupChevronToggles() {
        bindSection(
            chevron = binding.patientAlertsChevronSugar,
            config = binding.patientAlertsConfigSugar,
            expanded = isSugarExpanded,
            onToggle = { isSugarExpanded = it }
        )
        bindSection(
            chevron = binding.patientAlertsChevronInsulin,
            config = binding.patientAlertsConfigInsulin,
            expanded = isInsulinExpanded,
            onToggle = { isInsulinExpanded = it }
        )
        bindSection(
            chevron = binding.patientAlertsChevronPressure,
            config = binding.patientAlertsConfigPressure,
            expanded = isPressureExpanded,
            onToggle = { isPressureExpanded = it }
        )
        bindSection(
            chevron = binding.patientAlertsChevronPulse,
            config = binding.patientAlertsConfigPulse,
            expanded = isPulseExpanded,
            onToggle = { isPulseExpanded = it }
        )
    }

    private fun setupPressureConfigUi() {
        binding.patientAlertsPressureEnabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPressureEnabled(isChecked)
        }

        binding.patientAlertsPressureSaveButton.setOnClickListener {
            clearErrors()
            val sysLow = parseNullableDouble(binding.patientAlertsPressureSysLow)
            val sysHigh = parseNullableDouble(binding.patientAlertsPressureSysHigh)
            val diaLow = parseNullableDouble(binding.patientAlertsPressureDiaLow)
            val diaHigh = parseNullableDouble(binding.patientAlertsPressureDiaHigh)

            val ok = validatePressure(sysLow, sysHigh, diaLow, diaHigh)
            if (!ok) return@setOnClickListener

            viewModel.savePressureConfig(
                systolicMin = sysLow,
                systolicMax = sysHigh,
                diastolicMin = diaLow,
                diastolicMax = diaHigh
            )
        }

        viewModel.pressureConfig.observe(viewLifecycleOwner) { ui ->
            binding.patientAlertsPressureEnabledSwitch.setOnCheckedChangeListener(null)
            binding.patientAlertsPressureEnabledSwitch.isChecked = ui.enabled
            binding.patientAlertsPressureEnabledSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setPressureEnabled(isChecked)
            }

            setIfNotFocused(binding.patientAlertsPressureSysLow, ui.systolicMin?.toInt()?.toString())
            setIfNotFocused(binding.patientAlertsPressureSysHigh, ui.systolicMax?.toInt()?.toString())
            setIfNotFocused(binding.patientAlertsPressureDiaLow, ui.diastolicMin?.toInt()?.toString())
            setIfNotFocused(binding.patientAlertsPressureDiaHigh, ui.diastolicMax?.toInt()?.toString())
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { ok ->
            when (ok) {
                true -> Toast.makeText(requireContext(), getString(R.string.patient_alerts_pressure_toast_saved), Toast.LENGTH_SHORT).show()
                false -> Toast.makeText(requireContext(), getString(R.string.patient_alerts_pressure_toast_save_error), Toast.LENGTH_SHORT).show()
                null -> Unit
            }
            if (ok != null) viewModel.onSaveResultHandled()
        }
    }

    private fun validatePressure(
        systolicMin: Double?,
        systolicMax: Double?,
        diastolicMin: Double?,
        diastolicMax: Double?
    ): Boolean {
        var ok = true
        ok = ok && validatePair(
            low = systolicMin,
            high = systolicMax,
            lowLayout = binding.patientAlertsPressureSysLowLayout,
            highLayout = binding.patientAlertsPressureSysHighLayout
        )
        ok = ok && validatePair(
            low = diastolicMin,
            high = diastolicMax,
            lowLayout = binding.patientAlertsPressureDiaLowLayout,
            highLayout = binding.patientAlertsPressureDiaHighLayout
        )

        ok = ok && validateRange(systolicMin, 50.0, 260.0, binding.patientAlertsPressureSysLowLayout)
        ok = ok && validateRange(systolicMax, 50.0, 260.0, binding.patientAlertsPressureSysHighLayout)
        ok = ok && validateRange(diastolicMin, 30.0, 150.0, binding.patientAlertsPressureDiaLowLayout)
        ok = ok && validateRange(diastolicMax, 30.0, 150.0, binding.patientAlertsPressureDiaHighLayout)
        return ok
    }

    private fun validatePair(
        low: Double?,
        high: Double?,
        lowLayout: TextInputLayout,
        highLayout: TextInputLayout
    ): Boolean {
        if (low == null || high == null) return true
        if (low < high) return true
        lowLayout.error = "Low musi byc < high"
        highLayout.error = "High musi byc > low"
        return false
    }

    private fun validateRange(
        value: Double?,
        min: Double,
        max: Double,
        layout: TextInputLayout
    ): Boolean {
        if (value == null) return true
        if (value in min..max) return true
        layout.error = "Zakres: ${min.toInt()}-${max.toInt()}"
        return false
    }

    private fun clearErrors() {
        binding.patientAlertsPressureSysLowLayout.error = null
        binding.patientAlertsPressureSysHighLayout.error = null
        binding.patientAlertsPressureDiaLowLayout.error = null
        binding.patientAlertsPressureDiaHighLayout.error = null
    }

    private fun parseNullableDouble(edit: TextInputEditText): Double? {
        val text = edit.text?.toString()?.trim().orEmpty()
        if (text.isBlank()) return null
        return text.toDoubleOrNull()
    }

    private fun setIfNotFocused(edit: TextInputEditText, value: String?) {
        if (edit.hasFocus()) return
        edit.setText(value.orEmpty())
    }

    private fun bindSection(
        chevron: ImageView,
        config: View,
        expanded: Boolean,
        onToggle: (Boolean) -> Unit
    ) {
        renderSection(chevron, config, expanded, animate = false)
        chevron.setOnClickListener {
            val newExpanded = !config.isVisible
            onToggle(newExpanded)
            renderSection(chevron, config, newExpanded, animate = true)
        }
    }

    private fun renderSection(
        chevron: ImageView,
        config: View,
        expanded: Boolean,
        animate: Boolean
    ) {
        val targetRotation = if (expanded) 180f else 0f
        chevron.animate().cancel()
        if (animate) {
            chevron.animate()
                .rotation(targetRotation)
                .setDuration(CHEVRON_ANIM_MS)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        } else {
            chevron.rotation = targetRotation
        }

        config.animate().cancel()
        if (!animate) {
            config.isVisible = expanded
            config.alpha = 1f
            return
        }

        if (expanded) {
            config.isVisible = true
            config.alpha = 0f
            config.animate()
                .alpha(1f)
                .setDuration(CONFIG_FADE_ANIM_MS)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        } else {
            config.animate()
                .alpha(0f)
                .setDuration(CONFIG_FADE_ANIM_MS)
                .setInterpolator(FastOutSlowInInterpolator())
                .withEndAction {
                    config.isVisible = false
                    config.alpha = 1f
                }
                .start()
        }
    }

    private companion object {
        const val KEY_SUGAR_EXPANDED = "KEY_SUGAR_EXPANDED"
        const val KEY_INSULIN_EXPANDED = "KEY_INSULIN_EXPANDED"
        const val KEY_PRESSURE_EXPANDED = "KEY_PRESSURE_EXPANDED"
        const val KEY_PULSE_EXPANDED = "KEY_PULSE_EXPANDED"

        const val CHEVRON_ANIM_MS = 180L
        const val CONFIG_FADE_ANIM_MS = 160L
    }
}

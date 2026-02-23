package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import zdrowy.senior.io.ui.databinding.FragmentPatientAlertsBinding
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.domain.measurement.MeasurementType
import zdrowy.senior.io.ui.views.AlertNotificationsConfigView

class PatientAlertsFragment : Fragment() {
    private var _binding: FragmentPatientAlertsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientAlertsViewModel by viewModel()

    private var isSugarExpanded: Boolean = true
    private var isInsulinExpanded: Boolean = false
    private var isPressureExpanded: Boolean = false
    private var isPulseExpanded: Boolean = false
    private var suppressPressureInput = false
    private var suppressSugarInput = false
    private var suppressPulseInput = false

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
        setupSugarConfigUi()
        setupPressureConfigUi()
        setupPulseConfigUi()
        setupNotificationsConfigUi()
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
            viewModel.setEnabled(MeasurementType.PRESSURE, isChecked)
        }

        fun onPressureChanged() {
            if (suppressPressureInput) return
            clearPressureErrors()
            val sysLow = parseNullableDouble(binding.patientAlertsPressureSysLow)
            val sysHigh = parseNullableDouble(binding.patientAlertsPressureSysHigh)
            val diaLow = parseNullableDouble(binding.patientAlertsPressureDiaLow)
            val diaHigh = parseNullableDouble(binding.patientAlertsPressureDiaHigh)

            val ok = validatePressure(sysLow, sysHigh, diaLow, diaHigh)
            if (!ok) return

            viewModel.onPressureFieldsChanged(
                systolicMin = sysLow,
                systolicMax = sysHigh,
                diastolicMin = diaLow,
                diastolicMax = diaHigh
            )
        }

        binding.patientAlertsPressureSysLow.doAfterTextChanged { onPressureChanged() }
        binding.patientAlertsPressureSysHigh.doAfterTextChanged { onPressureChanged() }
        binding.patientAlertsPressureDiaLow.doAfterTextChanged { onPressureChanged() }
        binding.patientAlertsPressureDiaHigh.doAfterTextChanged { onPressureChanged() }

        viewModel.pressureConfig.observe(viewLifecycleOwner) { ui ->
            suppressPressureInput = true
            binding.patientAlertsPressureEnabledSwitch.setOnCheckedChangeListener(null)
            binding.patientAlertsPressureEnabledSwitch.isChecked = ui.enabled
            binding.patientAlertsPressureEnabledSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setEnabled(MeasurementType.PRESSURE, isChecked)
            }

            setIfNotFocused(binding.patientAlertsPressureSysLow, ui.systolicMin?.toInt()?.toString())
            setIfNotFocused(binding.patientAlertsPressureSysHigh, ui.systolicMax?.toInt()?.toString())
            setIfNotFocused(binding.patientAlertsPressureDiaLow, ui.diastolicMin?.toInt()?.toString())
            setIfNotFocused(binding.patientAlertsPressureDiaHigh, ui.diastolicMax?.toInt()?.toString())
            suppressPressureInput = false
        }
    }

    private fun setupSugarConfigUi() {
        binding.patientAlertsSugarEnabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setEnabled(MeasurementType.SUGAR, isChecked)
        }

        fun onSugarChanged() {
            if (suppressSugarInput) return
            clearSugarErrors()
            val low = parseNullableDouble(binding.patientAlertsSugarLow)
            val high = parseNullableDouble(binding.patientAlertsSugarHigh)
            val dropDelta = parseNullableDouble(binding.patientAlertsSugarDropDelta)
            val dropWindow = parseNullableInt(binding.patientAlertsSugarDropWindow)

            val ok = validatePair(
                low = low,
                high = high,
                lowLayout = binding.patientAlertsSugarLowLayout,
                highLayout = binding.patientAlertsSugarHighLayout
            )
            if (!ok) return

            viewModel.onSugarFieldsChanged(
                min = low,
                max = high,
                dropDelta = dropDelta,
                dropWindowMinutes = dropWindow
            )
        }

        binding.patientAlertsSugarLow.doAfterTextChanged { onSugarChanged() }
        binding.patientAlertsSugarHigh.doAfterTextChanged { onSugarChanged() }
        binding.patientAlertsSugarDropDelta.doAfterTextChanged { onSugarChanged() }
        binding.patientAlertsSugarDropWindow.doAfterTextChanged { onSugarChanged() }

        viewModel.sugarConfig.observe(viewLifecycleOwner) { ui ->
            suppressSugarInput = true
            binding.patientAlertsSugarEnabledSwitch.setOnCheckedChangeListener(null)
            binding.patientAlertsSugarEnabledSwitch.isChecked = ui.enabled
            binding.patientAlertsSugarEnabledSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setEnabled(MeasurementType.SUGAR, isChecked)
            }

            setIfNotFocused(binding.patientAlertsSugarLow, ui.min?.toInt()?.toString())
            setIfNotFocused(binding.patientAlertsSugarHigh, ui.max?.toInt()?.toString())
            setIfNotFocused(binding.patientAlertsSugarDropDelta, ui.dropDelta?.toInt()?.toString())
            setIfNotFocused(binding.patientAlertsSugarDropWindow, ui.dropWindowMinutes?.toString())
            suppressSugarInput = false
        }
    }

    private fun setupPulseConfigUi() {
        binding.patientAlertsPulseEnabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setEnabled(MeasurementType.PULSE, isChecked)
        }

        fun onPulseChanged() {
            if (suppressPulseInput) return
            clearPulseErrors()
            val low = parseNullableDouble(binding.patientAlertsPulseLow)
            val high = parseNullableDouble(binding.patientAlertsPulseHigh)

            val ok = validatePair(
                low = low,
                high = high,
                lowLayout = binding.patientAlertsPulseLowLayout,
                highLayout = binding.patientAlertsPulseHighLayout
            )
            if (!ok) return

            viewModel.onPulseFieldsChanged(min = low, max = high)
        }

        binding.patientAlertsPulseLow.doAfterTextChanged { onPulseChanged() }
        binding.patientAlertsPulseHigh.doAfterTextChanged { onPulseChanged() }

        viewModel.pulseConfig.observe(viewLifecycleOwner) { ui ->
            suppressPulseInput = true
            binding.patientAlertsPulseEnabledSwitch.setOnCheckedChangeListener(null)
            binding.patientAlertsPulseEnabledSwitch.isChecked = ui.enabled
            binding.patientAlertsPulseEnabledSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setEnabled(MeasurementType.PULSE, isChecked)
            }

            setIfNotFocused(binding.patientAlertsPulseLow, ui.min?.toInt()?.toString())
            setIfNotFocused(binding.patientAlertsPulseHigh, ui.max?.toInt()?.toString())
            suppressPulseInput = false
        }
    }

    private fun setupNotificationsConfigUi() {
        bindNotificationsCallbacks(binding.patientAlertsNotificationsSugar, MeasurementType.SUGAR)
        bindNotificationsCallbacks(binding.patientAlertsNotificationsInsulin, MeasurementType.INSULIN)
        bindNotificationsCallbacks(binding.patientAlertsNotificationsPressure, MeasurementType.PRESSURE)
        bindNotificationsCallbacks(binding.patientAlertsNotificationsPulse, MeasurementType.PULSE)

        viewModel.notifications.observe(viewLifecycleOwner) { map ->
            renderNotifications(binding.patientAlertsNotificationsSugar, map[MeasurementType.SUGAR])
            renderNotifications(binding.patientAlertsNotificationsInsulin, map[MeasurementType.INSULIN])
            renderNotifications(binding.patientAlertsNotificationsPressure, map[MeasurementType.PRESSURE])
            renderNotifications(binding.patientAlertsNotificationsPulse, map[MeasurementType.PULSE])
        }
    }

    private fun bindNotificationsCallbacks(view: AlertNotificationsConfigView, type: MeasurementType) {
        view.onChannelToggle = { channel, enabled -> viewModel.toggleChannel(type, channel, enabled) }
        view.onEnableAllChannels = { viewModel.enableAllChannels(type) }
        view.onDisableAllChannels = { viewModel.disableAllChannels(type) }

        view.onCaregiverToggle = { id, enabled -> viewModel.toggleCaregiver(type, id, enabled) }
        view.onSelectAllCaregivers = { viewModel.selectAllCaregivers(type) }
        view.onDeselectAllCaregivers = { viewModel.deselectAllCaregivers(type) }
    }

    private fun renderNotifications(view: AlertNotificationsConfigView, ui: AlertNotificationsUi?) {
        if (ui == null) return
        view.renderChannels(ui.channels)
        view.renderCaregivers(
            ui.caregivers.map {
                AlertNotificationsConfigView.CaregiverToggleUi(
                    id = it.id,
                    fullName = it.fullName,
                    phone = it.phone,
                    selected = it.selected
                )
            }
        )
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

    private fun clearPressureErrors() {
        binding.patientAlertsPressureSysLowLayout.error = null
        binding.patientAlertsPressureSysHighLayout.error = null
        binding.patientAlertsPressureDiaLowLayout.error = null
        binding.patientAlertsPressureDiaHighLayout.error = null
    }

    private fun clearSugarErrors() {
        binding.patientAlertsSugarLowLayout.error = null
        binding.patientAlertsSugarHighLayout.error = null
    }

    private fun clearPulseErrors() {
        binding.patientAlertsPulseLowLayout.error = null
        binding.patientAlertsPulseHighLayout.error = null
    }

    private fun parseNullableDouble(edit: TextInputEditText): Double? {
        val text = edit.text?.toString()?.trim().orEmpty()
        if (text.isBlank()) return null
        return text.toDoubleOrNull()
    }

    private fun parseNullableInt(edit: TextInputEditText): Int? {
        val text = edit.text?.toString()?.trim().orEmpty()
        if (text.isBlank()) return null
        return text.toIntOrNull()
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

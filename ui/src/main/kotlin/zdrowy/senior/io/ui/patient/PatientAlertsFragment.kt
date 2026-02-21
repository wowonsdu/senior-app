package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.fragment.findNavController
import zdrowy.senior.io.ui.databinding.FragmentPatientAlertsBinding

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
        viewModel.load()
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

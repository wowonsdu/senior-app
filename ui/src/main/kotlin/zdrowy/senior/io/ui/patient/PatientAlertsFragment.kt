package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import zdrowy.senior.io.ui.databinding.FragmentPatientAlertsBinding

class PatientAlertsFragment : Fragment() {
    private var _binding: FragmentPatientAlertsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientAlertsViewModel by viewModel()

    private var isSugarExpanded: Boolean = true

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
        setupSugarChevronCollapse()
        viewModel.load()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_SUGAR_EXPANDED, isSugarExpanded)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupSugarChevronCollapse() {
        val content = binding.patientAlertsScroll.getChildAt(0) as? ViewGroup ?: return
        val headerCard = content.getChildAt(1) as? MaterialCardView ?: return
        val configCard = content.getChildAt(2) as? MaterialCardView ?: return
        val chevron = findChevronInHeaderCard(headerCard) ?: return

        applySugarExpandedState(configCard, chevron, isSugarExpanded)
        chevron.setOnClickListener {
            isSugarExpanded = !isSugarExpanded
            applySugarExpandedState(configCard, chevron, isSugarExpanded)
        }
    }

    private fun applySugarExpandedState(
        configCard: MaterialCardView,
        chevron: ImageView,
        expanded: Boolean
    ) {
        configCard.isVisible = expanded
        chevron.rotation = if (expanded) 180f else 0f
    }

    private fun findChevronInHeaderCard(headerCard: MaterialCardView): ImageView? {
        val row = headerCard.getChildAt(0) as? ViewGroup ?: return null
        return row.getChildAt(row.childCount - 1) as? ImageView
    }

    private companion object {
        const val KEY_SUGAR_EXPANDED = "KEY_SUGAR_EXPANDED"
    }
}

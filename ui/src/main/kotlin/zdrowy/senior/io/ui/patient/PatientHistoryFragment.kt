package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.card.MaterialCardView
import zdrowy.senior.io.ui.databinding.FragmentPatientHistoryBinding
import zdrowy.senior.io.domain.measurement.MeasurementType
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import zdrowy.senior.io.ui.R

class PatientHistoryFragment : Fragment() {
    private var _binding: FragmentPatientHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientHistoryViewModel by viewModel()
    private val adapter = PatientMeasurementsAdapter()
    private val selectedTypes = mutableSetOf<MeasurementType>()
    private lateinit var tiles: Map<MeasurementType, TileUi>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientHistoryBinding.inflate(inflater, container, false)
        binding.patientHistoryList.layoutManager = LinearLayoutManager(requireContext())
        binding.patientHistoryList.adapter = adapter
        binding.patientHistoryToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.patientHistoryScroll.post {
            val availableHeight = binding.patientHistoryScroll.height
            val padding = binding.patientHistoryContent.paddingTop + binding.patientHistoryContent.paddingBottom
            val appBarHeight = binding.patientHistoryToolbar.height
            val listMargins = (binding.patientHistoryList.layoutParams as? ViewGroup.MarginLayoutParams)
                ?.let { it.topMargin + it.bottomMargin } ?: 0
            val targetHeight = (availableHeight - padding - appBarHeight - listMargins).coerceAtLeast(0)
            binding.patientHistoryList.updateLayoutParams<ViewGroup.LayoutParams> {
                height = targetHeight
            }
        }
        tiles = mapOf(
            MeasurementType.SUGAR to TileUi(
                card = binding.patientHistoryHeader.patientHistoryTileSugar,
                icon = binding.patientHistoryHeader.patientHistoryTileSugarIcon,
                label = binding.patientHistoryHeader.patientHistoryTileSugarLabel,
                selectedBackground = R.color.senior_info,
                selectedContent = R.color.white,
                unselectedBackground = R.color.white,
                unselectedContent = R.color.senior_info
            ),
            MeasurementType.INSULIN to TileUi(
                card = binding.patientHistoryHeader.patientHistoryTileInsulin,
                icon = binding.patientHistoryHeader.patientHistoryTileInsulinIcon,
                label = binding.patientHistoryHeader.patientHistoryTileInsulinLabel,
                selectedBackground = R.color.senior_secondary,
                selectedContent = R.color.white,
                unselectedBackground = R.color.white,
                unselectedContent = R.color.senior_secondary
            ),
            MeasurementType.PRESSURE to TileUi(
                card = binding.patientHistoryHeader.patientHistoryTilePressure,
                icon = binding.patientHistoryHeader.patientHistoryTilePressureIcon,
                label = binding.patientHistoryHeader.patientHistoryTilePressureLabel,
                selectedBackground = R.color.senior_danger,
                selectedContent = R.color.white,
                unselectedBackground = R.color.white,
                unselectedContent = R.color.senior_danger
            ),
            MeasurementType.PULSE to TileUi(
                card = binding.patientHistoryHeader.patientHistoryTilePulse,
                icon = binding.patientHistoryHeader.patientHistoryTilePulseIcon,
                label = binding.patientHistoryHeader.patientHistoryTilePulseLabel,
                selectedBackground = R.color.senior_purple,
                selectedContent = R.color.white,
                unselectedBackground = R.color.white,
                unselectedContent = R.color.senior_purple
            )
        )

        selectedTypes.clear()
        selectedTypes.addAll(MeasurementType.values())
        updateTileStates()

        tiles.forEach { (type, tile) ->
            tile.card.setOnClickListener { toggleType(type) }
        }

        viewModel.load()
        viewModel.measurements.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
        viewModel.filters.observe(viewLifecycleOwner) { state ->
            if (state.selectedTypes.isNotEmpty()) {
                selectedTypes.clear()
                selectedTypes.addAll(state.selectedTypes)
                updateTileStates()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun toggleType(type: MeasurementType) {
        if (selectedTypes.contains(type)) {
            if (selectedTypes.size == 1) return
            selectedTypes.remove(type)
        } else {
            selectedTypes.add(type)
        }
        updateTileStates()
        viewModel.setSelectedTypes(selectedTypes.toList())
    }

    private fun updateTileStates() {
        tiles.forEach { (type, tile) ->
            val selected = selectedTypes.contains(type)
            tile.card.setCardBackgroundColor(colorOf(if (selected) tile.selectedBackground else tile.unselectedBackground))
            tile.icon.setColorFilter(colorOf(if (selected) tile.selectedContent else tile.unselectedContent))
            tile.label.setTextColor(colorOf(if (selected) tile.selectedContent else tile.unselectedContent))
            tile.card.strokeWidth = if (selected) 0 else resources.getDimensionPixelSize(R.dimen.stroke_width)
            tile.card.setStrokeColor(colorOf(R.color.senior_outline))
        }
    }

    private fun colorOf(colorRes: Int): Int {
        return ContextCompat.getColor(requireContext(), colorRes)
    }

    private data class TileUi(
        val card: MaterialCardView,
        val icon: ImageView,
        val label: TextView,
        val selectedBackground: Int,
        val selectedContent: Int,
        val unselectedBackground: Int,
        val unselectedContent: Int
    )
}

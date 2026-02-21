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
import zdrowy.senior.io.domain.history.ChartSeries
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import zdrowy.senior.io.ui.R

class PatientHistoryFragment : Fragment() {
    private var _binding: FragmentPatientHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientHistoryViewModel by viewModel()
    private val adapter = PatientMeasurementsAdapter { item -> openEditDialog(item) }
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

        setupChart()
        viewModel.load()
        viewModel.measurements.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
        viewModel.chartSeries.observe(viewLifecycleOwner) { series ->
            renderChart(series)
        }
        viewModel.filters.observe(viewLifecycleOwner) { state ->
            if (state.selectedTypes.isNotEmpty()) {
                selectedTypes.clear()
                selectedTypes.addAll(state.selectedTypes)
                updateTileStates()
            }
        }
    }

    private fun openEditDialog(item: PatientMeasurementItemUi) {
        val args = bundleOf(
            ARG_MEASUREMENT_ID to item.id,
            ARG_MEASUREMENT_TIMESTAMP to item.timestamp
        )
        when (item.type) {
            MeasurementType.SUGAR -> {
                args.putFloat(ARG_MEASUREMENT_VALUE, (item.value ?: 0.0).toFloat())
                findNavController().navigate(R.id.patientSugarDialogFragment, args)
            }
            MeasurementType.INSULIN -> {
                args.putFloat(ARG_MEASUREMENT_VALUE, (item.value ?: 0.0).toFloat())
                findNavController().navigate(R.id.patientInsulinDialogFragment, args)
            }
            MeasurementType.PRESSURE -> {
                args.putInt(ARG_MEASUREMENT_SYSTOLIC, item.systolic ?: 0)
                args.putInt(ARG_MEASUREMENT_DIASTOLIC, item.diastolic ?: 0)
                findNavController().navigate(R.id.patientPressureDialogFragment, args)
            }
            MeasurementType.PULSE -> {
                args.putFloat(ARG_MEASUREMENT_VALUE, (item.value ?: 0.0).toFloat())
                findNavController().navigate(R.id.patientPulseDialogFragment, args)
            }
        }
    }

    private fun renderChart(series: List<ChartSeries>) {
        val chart = binding.patientHistoryHeader.patientHistoryChartView
        if (series.isEmpty()) {
            chart.clear()
            chart.invalidate()
            return
        }
        val dataSets = series.mapNotNull { chartSeries ->
            if (chartSeries.points.isEmpty()) return@mapNotNull null
            val entries = chartSeries.points.mapIndexed { index, point ->
                Entry(index.toFloat(), point.value.toFloat())
            }
            val primary = LineDataSet(entries, null).apply {
                color = colorOf(colorFor(chartSeries.type))
                lineWidth = 2f
                setDrawValues(false)
                setDrawCircles(false)
                setDrawCircleHole(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            if (chartSeries.type == MeasurementType.PRESSURE && chartSeries.secondaryPoints.isNotEmpty()) {
                val secondaryEntries = chartSeries.secondaryPoints.mapIndexed { index, point ->
                    Entry(index.toFloat(), point.value.toFloat())
                }
                val secondaryColor = colorOf(colorFor(chartSeries.type))
                val secondary = LineDataSet(secondaryEntries, null).apply {
                    color = secondaryColor
                    lineWidth = 2f
                    setDrawValues(false)
                    setDrawCircles(false)
                    setDrawCircleHole(false)
                    enableDashedLine(10f, 6f, 0f)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }
                listOf(primary, secondary)
            } else {
                listOf(primary)
            }
        }
        chart.data = LineData(dataSets.flatten())
        chart.invalidate()
    }

    private fun colorFor(type: MeasurementType): Int {
        return when (type) {
            MeasurementType.SUGAR -> R.color.senior_info
            MeasurementType.INSULIN -> R.color.senior_secondary
            MeasurementType.PRESSURE -> R.color.senior_danger
            MeasurementType.PULSE -> R.color.senior_purple
        }
    }

    private fun setupChart() {
        val chart = binding.patientHistoryHeader.patientHistoryChartView
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setNoDataText(getString(R.string.patient_history_chart_placeholder))
        chart.setNoDataTextColor(colorOf(R.color.senior_text_tertiary))
        chart.setTouchEnabled(false)
        chart.axisRight.isEnabled = false
        chart.axisLeft.setDrawGridLines(true)
        chart.axisLeft.textColor = colorOf(R.color.senior_text_tertiary)
        chart.axisLeft.axisLineColor = colorOf(R.color.senior_outline)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.textColor = colorOf(R.color.senior_text_tertiary)
        chart.xAxis.axisLineColor = colorOf(R.color.senior_outline)
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

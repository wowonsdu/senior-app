package zdrowy.senior.io.ui.patient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import zdrowy.senior.io.domain.measurement.Measurement
import zdrowy.senior.io.domain.measurement.MeasurementType
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.ItemPatientMeasurementBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PatientMeasurementsAdapter : ListAdapter<Measurement, PatientMeasurementsAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPatientMeasurementBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemPatientMeasurementBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val formatter = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())

        fun bind(measurement: Measurement) {
            binding.itemMeasurementRow.setTitle(formatTitle(measurement))
            binding.itemMeasurementRow.setSubtitle(formatter.format(Date(measurement.timestamp)))
            binding.itemMeasurementRow.setIconRes(iconFor(measurement.type))
            binding.itemMeasurementRow.setIconTint(
                ContextCompat.getColor(binding.root.context, tintFor(measurement.type))
            )
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

    private class Diff : DiffUtil.ItemCallback<Measurement>() {
        override fun areItemsTheSame(oldItem: Measurement, newItem: Measurement): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Measurement, newItem: Measurement): Boolean {
            return oldItem == newItem
        }
    }
}

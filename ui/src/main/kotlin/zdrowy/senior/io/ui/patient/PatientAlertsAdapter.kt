package zdrowy.senior.io.ui.patient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import zdrowy.senior.io.domain.alert.AlertSetting
import zdrowy.senior.io.ui.databinding.ItemPatientAlertBinding

class PatientAlertsAdapter : ListAdapter<AlertSetting, PatientAlertsAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPatientAlertBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemPatientAlertBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AlertSetting) {
            val status = if (item.enabled) "wlaczony" else "wylaczony"
            val min = item.min?.toString() ?: "-"
            val max = item.max?.toString() ?: "-"
            binding.alertName.text = item.type.name.lowercase().replaceFirstChar { it.uppercase() }
            binding.alertStatus.text = "Status: $status"
            binding.alertThresholds.text = "Min: $min \u2022 Max: $max"
        }
    }

    private class Diff : DiffUtil.ItemCallback<AlertSetting>() {
        override fun areItemsTheSame(oldItem: AlertSetting, newItem: AlertSetting): Boolean =
            oldItem.type == newItem.type

        override fun areContentsTheSame(oldItem: AlertSetting, newItem: AlertSetting): Boolean =
            oldItem == newItem
    }
}

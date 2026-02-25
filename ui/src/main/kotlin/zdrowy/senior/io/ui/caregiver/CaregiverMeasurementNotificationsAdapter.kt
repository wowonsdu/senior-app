package zdrowy.senior.io.ui.caregiver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.caregiver.model.CaregiverMeasurementNotificationUi
import zdrowy.senior.io.ui.databinding.ItemCaregiverMeasurementNotificationBinding

class CaregiverMeasurementNotificationsAdapter(
    private val onMarkRead: (CaregiverMeasurementNotificationUi) -> Unit
) : ListAdapter<CaregiverMeasurementNotificationUi, CaregiverMeasurementNotificationsAdapter.ViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCaregiverMeasurementNotificationBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, onMarkRead)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemCaregiverMeasurementNotificationBinding,
        private val onMarkRead: (CaregiverMeasurementNotificationUi) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CaregiverMeasurementNotificationUi) {
            val context = binding.root.context
            val chipColor = ContextCompat.getColor(context, item.chipColorRes)
            val iconColor = ContextCompat.getColor(context, item.iconTintRes)
            val cardColor = ContextCompat.getColor(
                context,
                if (item.isRead) R.color.senior_surface else R.color.senior_warning_bg
            )
            val strokeColor = ContextCompat.getColor(
                context,
                if (item.isRead) R.color.senior_outline else R.color.senior_warning_outline
            )

            binding.caregiverMeasurementCard.setCardBackgroundColor(cardColor)
            binding.caregiverMeasurementCard.strokeColor = strokeColor
            binding.caregiverMeasurementIconCard.setCardBackgroundColor(iconColor)
            binding.caregiverMeasurementIcon.setImageResource(item.iconRes)
            binding.caregiverMeasurementTypeChip.setCardBackgroundColor(chipColor)

            binding.caregiverMeasurementName.text = item.patientName
            binding.caregiverMeasurementType.text = item.typeLabel
            binding.caregiverMeasurementValue.text = item.valueLabel
            binding.caregiverMeasurementTime.text = item.timeLabel

            binding.caregiverMeasurementCheckbox.setOnCheckedChangeListener(null)
            binding.caregiverMeasurementCheckbox.isChecked = false
            binding.caregiverMeasurementCheckbox.visibility = if (item.isRead) View.GONE else View.VISIBLE
            binding.caregiverMeasurementCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    onMarkRead(item)
                }
            }
        }
    }

    private class Diff : DiffUtil.ItemCallback<CaregiverMeasurementNotificationUi>() {
        override fun areItemsTheSame(
            oldItem: CaregiverMeasurementNotificationUi,
            newItem: CaregiverMeasurementNotificationUi
        ): Boolean = oldItem.id == newItem.id && oldItem.patientUid == newItem.patientUid

        override fun areContentsTheSame(
            oldItem: CaregiverMeasurementNotificationUi,
            newItem: CaregiverMeasurementNotificationUi
        ): Boolean = oldItem == newItem
    }
}

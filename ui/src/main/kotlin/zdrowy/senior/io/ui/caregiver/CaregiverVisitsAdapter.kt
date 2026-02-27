package zdrowy.senior.io.ui.caregiver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.caregiver.model.CaregiverVisitItemUi
import zdrowy.senior.io.ui.databinding.ItemCaregiverVisitBinding

class CaregiverVisitsAdapter(
    private val onComplete: (CaregiverVisitItemUi) -> Unit,
    private val onEdit: (CaregiverVisitItemUi) -> Unit,
    private val onDelete: (CaregiverVisitItemUi) -> Unit
) : ListAdapter<CaregiverVisitItemUi, CaregiverVisitsAdapter.ViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCaregiverVisitBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, onComplete, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemCaregiverVisitBinding,
        private val onComplete: (CaregiverVisitItemUi) -> Unit,
        private val onEdit: (CaregiverVisitItemUi) -> Unit,
        private val onDelete: (CaregiverVisitItemUi) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CaregiverVisitItemUi) {
            val context = itemView.context
            binding.caregiverVisitPatient.text = item.patientName
            binding.caregiverVisitTitle.text = item.title
            binding.caregiverVisitDatetime.text = context.getString(
                R.string.caregiver_visits_datetime_format,
                item.dateLabel,
                item.timeLabel
            )
            binding.caregiverVisitLocation.text = if (item.locationLabel.isBlank()) {
                context.getString(R.string.caregiver_visits_location_empty)
            } else {
                context.getString(R.string.caregiver_visits_location_format, item.locationLabel)
            }

            if (item.reminderLabel.isNullOrBlank()) {
                binding.caregiverVisitReminder.visibility = View.GONE
            } else {
                binding.caregiverVisitReminder.visibility = View.VISIBLE
                binding.caregiverVisitReminder.text = context.getString(
                    R.string.caregiver_visits_reminder_format,
                    item.reminderLabel
                )
            }

            if (item.isCompleted) {
                binding.caregiverVisitStatus.text = context.getString(R.string.caregiver_visits_status_completed)
                binding.caregiverVisitStatusChip.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.senior_secondary)
                )
                binding.caregiverVisitComplete.visibility = View.GONE
            } else {
                binding.caregiverVisitStatus.text = context.getString(R.string.caregiver_visits_status_upcoming)
                binding.caregiverVisitStatusChip.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.senior_warning)
                )
                binding.caregiverVisitComplete.visibility = View.VISIBLE
            }

            binding.caregiverVisitComplete.setOnClickListener { onComplete(item) }
            binding.caregiverVisitEdit.setOnClickListener { onEdit(item) }
            binding.caregiverVisitDelete.setOnClickListener { onDelete(item) }
        }
    }

    private class Diff : DiffUtil.ItemCallback<CaregiverVisitItemUi>() {
        override fun areItemsTheSame(
            oldItem: CaregiverVisitItemUi,
            newItem: CaregiverVisitItemUi
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: CaregiverVisitItemUi,
            newItem: CaregiverVisitItemUi
        ): Boolean = oldItem == newItem
    }
}

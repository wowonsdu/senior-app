package zdrowy.senior.io.ui.caregiver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import zdrowy.senior.io.ui.caregiver.model.CaregiverDependentTileUiModel
import zdrowy.senior.io.ui.databinding.ItemCaregiverDependentBinding

class CaregiverDependentsAdapter(
    private val onSelect: (CaregiverDependentTileUiModel) -> Unit
) : ListAdapter<CaregiverDependentTileUiModel, CaregiverDependentsAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCaregiverDependentBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, onSelect)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemCaregiverDependentBinding,
        private val onSelect: (CaregiverDependentTileUiModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CaregiverDependentTileUiModel) {
            binding.caregiverDependentAvatar.text = item.avatar
            if (item.isSelf) {
                binding.caregiverDependentName.text = itemView.context.getString(
                    zdrowy.senior.io.ui.R.string.caregiver_dependents_self_label
                )
            } else {
                binding.caregiverDependentName.text = item.fullName
            }
            binding.caregiverDependentPhone.text = item.phone
            binding.root.setOnClickListener { onSelect(item) }
            val unread = item.unreadCount
            binding.caregiverDependentBell.visibility = if (unread > 0) View.VISIBLE else View.GONE
            binding.caregiverDependentBadge.text = unread.toString()
        }
    }

    private class Diff : DiffUtil.ItemCallback<CaregiverDependentTileUiModel>() {
        override fun areItemsTheSame(
            oldItem: CaregiverDependentTileUiModel,
            newItem: CaregiverDependentTileUiModel
        ): Boolean = oldItem.uid == newItem.uid

        override fun areContentsTheSame(
            oldItem: CaregiverDependentTileUiModel,
            newItem: CaregiverDependentTileUiModel
        ): Boolean = oldItem == newItem
    }
}

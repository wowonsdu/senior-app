package zdrowy.senior.io.ui.patient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import zdrowy.senior.io.ui.databinding.ItemPatientMeasurementBinding

class PatientMeasurementsAdapter(
    private val onItemClick: (PatientMeasurementItemUi) -> Unit
) : ListAdapter<PatientMeasurementItemUi, PatientMeasurementsAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPatientMeasurementBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class ViewHolder(
        private val binding: ItemPatientMeasurementBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: PatientMeasurementItemUi,
            onItemClick: (PatientMeasurementItemUi) -> Unit
        ) {
            binding.itemMeasurementRow.setTitle(item.title)
            binding.itemMeasurementRow.setSubtitle(item.subtitle)
            binding.itemMeasurementRow.setIconRes(item.iconRes)
            binding.itemMeasurementRow.setIconTint(
                ContextCompat.getColor(binding.root.context, item.iconTintRes)
            )
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    private class Diff : DiffUtil.ItemCallback<PatientMeasurementItemUi>() {
        override fun areItemsTheSame(
            oldItem: PatientMeasurementItemUi,
            newItem: PatientMeasurementItemUi
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PatientMeasurementItemUi,
            newItem: PatientMeasurementItemUi
        ): Boolean {
            return oldItem == newItem
        }
    }
}

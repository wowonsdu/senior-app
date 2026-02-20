package zdrowy.senior.io.ui.patient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import zdrowy.senior.io.domain.settings.Medication
import zdrowy.senior.io.ui.databinding.ItemPatientMedicationBinding

class PatientMedicationsAdapter(
    private val onItemClick: (Medication) -> Unit = {}
) : ListAdapter<Medication, PatientMedicationsAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPatientMedicationBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPatientMedicationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Medication) {
            binding.root.setOnClickListener { onItemClick(item) }
            binding.medicationName.text = item.name
            binding.medicationDosage.text = item.dosage
            binding.medicationSchedule.text = item.schedule
        }
    }

    private class Diff : DiffUtil.ItemCallback<Medication>() {
        override fun areItemsTheSame(oldItem: Medication, newItem: Medication): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Medication, newItem: Medication): Boolean = oldItem == newItem
    }
}

package zdrowy.senior.io.ui.patient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import zdrowy.senior.io.ui.databinding.ItemPatientDiseaseBinding
import zdrowy.senior.io.domain.settings.Disease

class PatientDiseasesAdapter : ListAdapter<Disease, PatientDiseasesAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPatientDiseaseBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemPatientDiseaseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Disease) {
            binding.diseaseName.text = item.name
            binding.diseaseSeverity.text = item.severity
            binding.diseaseNotes.text = item.notes
        }
    }

    private class Diff : DiffUtil.ItemCallback<Disease>() {
        override fun areItemsTheSame(oldItem: Disease, newItem: Disease): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Disease, newItem: Disease): Boolean = oldItem == newItem
    }
}

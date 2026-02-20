package zdrowy.senior.io.ui.patient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import zdrowy.senior.io.ui.databinding.ItemPatientDiseaseBinding
import zdrowy.senior.io.domain.settings.Disease
import zdrowy.senior.io.ui.R

class PatientDiseasesAdapter(
    private val onItemClick: (Disease) -> Unit = {}
) : ListAdapter<Disease, PatientDiseasesAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPatientDiseaseBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPatientDiseaseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Disease) {
            val context = binding.root.context
            binding.root.setOnClickListener { onItemClick(item) }
            binding.diseaseName.text = item.name
            binding.diseaseSeverity.text = item.severity
            binding.diseaseNotes.text = item.notes
            val severityStyle = resolveSeverityStyle(item.severity)
            binding.diseaseSeverityChipText.text = severityStyle.label
            val color = ContextCompat.getColor(context, severityStyle.colorRes)
            binding.diseaseSeverityChip.setCardBackgroundColor(color)
            binding.diseaseIcon.setColorFilter(color)
        }
    }

    private data class SeverityStyle(
        val label: String,
        val colorRes: Int
    )

    private fun resolveSeverityStyle(severity: String): SeverityStyle {
        val normalized = severity.lowercase()
        return when {
            normalized.contains("ciezk") -> SeverityStyle("CIEZKI", R.color.senior_danger)
            normalized.contains("sredn") || normalized.contains("umiark") ->
                SeverityStyle("SREDNI", R.color.senior_warning)
            normalized.contains("lagodn") || normalized.contains("lek") ->
                SeverityStyle("LAGODNY", R.color.senior_secondary)
            else -> SeverityStyle("INFO", R.color.senior_info)
        }
    }

    private class Diff : DiffUtil.ItemCallback<Disease>() {
        override fun areItemsTheSame(oldItem: Disease, newItem: Disease): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Disease, newItem: Disease): Boolean = oldItem == newItem
    }
}

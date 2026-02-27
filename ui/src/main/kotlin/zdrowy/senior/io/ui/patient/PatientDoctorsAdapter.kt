package zdrowy.senior.io.ui.patient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import zdrowy.senior.io.domain.agent.Agent
import zdrowy.senior.io.ui.databinding.ItemPatientAgentBinding

class PatientDoctorsAdapter(
    private val onItemClick: (Agent) -> Unit = {}
) : ListAdapter<Agent, PatientDoctorsAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPatientAgentBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemPatientAgentBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(doctor: Agent) {
            binding.root.setOnClickListener { onItemClick(doctor) }
            binding.agentName.text = doctor.fullName
            binding.agentRole.text = doctor.specialization.orEmpty().ifBlank { "Lekarz" }
            binding.agentContact.text = "${doctor.phone} • ${doctor.email}"
        }
    }

    private class Diff : DiffUtil.ItemCallback<Agent>() {
        override fun areItemsTheSame(oldItem: Agent, newItem: Agent): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Agent, newItem: Agent): Boolean = oldItem == newItem
    }
}

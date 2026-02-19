package zdrowy.senior.io.ui.patient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import zdrowy.senior.io.domain.agent.Agent
import zdrowy.senior.io.ui.databinding.ItemPatientAgentBinding

class PatientAgentsAdapter : ListAdapter<Agent, PatientAgentsAdapter.ViewHolder>(Diff()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPatientAgentBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemPatientAgentBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(agent: Agent) {
            binding.agentName.text = agent.fullName
            binding.agentRole.text = agent.role.name.lowercase().replaceFirstChar { it.uppercase() }
            binding.agentContact.text = "${agent.phone} • ${agent.email}"
        }
    }

    private class Diff : DiffUtil.ItemCallback<Agent>() {
        override fun areItemsTheSame(oldItem: Agent, newItem: Agent): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Agent, newItem: Agent): Boolean = oldItem == newItem
    }
}

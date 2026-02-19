package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import org.koin.android.ext.android.get
import zdrowy.senior.io.domain.agent.AgentRole
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientAgentsBinding

class PatientAgentsFragment : Fragment() {
    private var _binding: FragmentPatientAgentsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PatientAgentsViewModel> {
        PatientAgentsViewModel.Factory(get())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientAgentsBinding.inflate(inflater, container, false)
        binding.patientAgentsToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientAgentsAddAgent.setOnClickListener {
            findNavController().navigate(R.id.action_patientAgents_to_addAgent)
        }
        binding.patientAgentsAddDoctor.setOnClickListener {
            findNavController().navigate(R.id.action_patientAgents_to_addDoctor)
        }
        binding.patientAgentsEmptyAgentsTitle.setOnClickListener {
            findNavController().navigate(R.id.action_patientAgents_to_editAgent)
        }
        binding.patientAgentsEmptyDoctorsTitle.setOnClickListener {
            findNavController().navigate(R.id.action_patientAgents_to_doctorCode)
        }
        binding.patientAgentsEmptyDoctorsTitle.setOnLongClickListener {
            findNavController().navigate(R.id.action_patientAgents_to_editDoctor)
            true
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadAgents()
        viewModel.agents.observe(viewLifecycleOwner) { agents ->
            val caregivers = agents.filter { it.role == AgentRole.CAREGIVER }
            val doctors = agents.filter { it.role == AgentRole.DOCTOR }

            binding.patientAgentsEmptyAgentsTitle.text = "Opiekunowie (${caregivers.size})"
            binding.patientAgentsEmptyDoctorsTitle.text = "Lekarze (${doctors.size})"

            binding.patientAgentsEmptyAgentsBody.text = if (caregivers.isEmpty()) {
                "Brak dodanych opiekunow."
            } else {
                caregivers.joinToString { it.fullName }
            }

            binding.patientAgentsEmptyDoctorsBody.text = if (doctors.isEmpty()) {
                "Brak dodanych lekarzy."
            } else {
                doctors.joinToString { it.fullName }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

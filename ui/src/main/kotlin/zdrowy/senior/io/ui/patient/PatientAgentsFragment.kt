package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientAgentsBinding

class PatientAgentsFragment : Fragment() {
    private var _binding: FragmentPatientAgentsBinding? = null
    private val binding get() = _binding!!

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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

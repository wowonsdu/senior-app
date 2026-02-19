package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientHomeBinding

class PatientHomeFragment : Fragment() {
    private var _binding: FragmentPatientHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientHomeBinding.inflate(inflater, container, false)
        binding.patientHomeTileSugar.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientSugarDialog)
        }
        binding.patientHomeTileInsulin.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientInsulinDialog)
        }
        binding.patientHomeTilePressure.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientPressureDialog)
        }
        binding.patientHomeTilePulse.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientPulseDialog)
        }
        binding.patientHomeActionHistory.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientHistory)
        }
        binding.patientHomeActionAgents.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientAgents)
        }
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

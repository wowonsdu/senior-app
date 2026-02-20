package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.get
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientHomeBinding

class PatientHomeFragment : Fragment() {
    private var _binding: FragmentPatientHomeBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val viewModel by viewModels<PatientHomeViewModel> {
        PatientHomeViewModel.Factory(get())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientHomeBinding.inflate(inflater, container, false)
        binding.patientHomeToolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.patient_home_logout) {
                auth.signOut()
                findNavController().navigate(R.id.action_patientHome_to_roleSelect)
                true
            } else {
                false
            }
        }
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
        binding.patientHomeActionSettings.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientSettings)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadRecent()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

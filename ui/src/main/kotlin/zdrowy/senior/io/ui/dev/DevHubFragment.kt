package zdrowy.senior.io.ui.dev

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentDevHubBinding

class DevHubFragment : Fragment() {
    private var _binding: FragmentDevHubBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDevHubBinding.inflate(inflater, container, false)
        binding.devHubRoleSelect.setOnClickListener {
            findNavController().navigate(R.id.action_devHub_to_roleSelect)
        }
        binding.devHubPatientLogin.setOnClickListener {
            findNavController().navigate(R.id.action_devHub_to_patientLogin)
        }
        binding.devHubCaregiverLogin.setOnClickListener {
            findNavController().navigate(R.id.action_devHub_to_caregiverLogin)
        }
        binding.devHubPatientSms.setOnClickListener {
            findNavController().navigate(R.id.action_devHub_to_patientSmsVerify)
        }
        binding.devHubCaregiverSms.setOnClickListener {
            findNavController().navigate(R.id.action_devHub_to_caregiverSmsVerify)
        }
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

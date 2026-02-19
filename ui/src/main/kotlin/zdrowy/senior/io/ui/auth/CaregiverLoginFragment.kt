package zdrowy.senior.io.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentCaregiverLoginBinding

class CaregiverLoginFragment : Fragment() {
    private var _binding: FragmentCaregiverLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaregiverLoginBinding.inflate(inflater, container, false)
        binding.caregiverLoginBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.caregiverLoginContinue.setOnClickListener {
            findNavController().navigate(R.id.action_caregiverLogin_to_caregiverSmsVerify)
        }
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

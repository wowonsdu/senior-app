package zdrowy.senior.io.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientLoginBinding

class PatientLoginFragment : Fragment() {
    private var _binding: FragmentPatientLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientLoginBinding.inflate(inflater, container, false)
        binding.patientLoginBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientLoginContinue.setOnClickListener {
            findNavController().navigate(R.id.action_patientLogin_to_patientSmsVerify)
        }
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

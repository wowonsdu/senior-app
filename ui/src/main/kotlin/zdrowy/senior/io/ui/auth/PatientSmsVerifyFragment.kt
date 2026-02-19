package zdrowy.senior.io.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientSmsVerifyBinding

class PatientSmsVerifyFragment : Fragment() {
    private var _binding: FragmentPatientSmsVerifyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientSmsVerifyBinding.inflate(inflater, container, false)
        binding.patientSmsBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientSmsConfirm.setOnClickListener {
            findNavController().navigate(R.id.action_patientSmsVerify_to_patientHome)
        }
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

package zdrowy.senior.io.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientSmsVerifyBinding

class PatientSmsVerifyFragment : Fragment() {
    private var _binding: FragmentPatientSmsVerifyBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientSmsVerifyBinding.inflate(inflater, container, false)
        val phone = arguments?.getString(PhoneAuthUi.ARG_PHONE_E164).orEmpty()
        val verificationId = arguments?.getString(PhoneAuthUi.ARG_VERIFICATION_ID).orEmpty()
        binding.patientSmsPhone.text = phone.ifBlank { getString(R.string.sms_verify_phone) }

        binding.patientSmsBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientSmsConfirm.setOnClickListener {
            confirmCode(verificationId)
        }
        binding.patientSmsResend.setOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun confirmCode(verificationId: String) {
        val code = binding.patientSmsCodeInput.editText?.text?.toString()?.trim().orEmpty()
        if (code.isBlank()) {
            binding.patientSmsCodeInput.error = "Pole wymagane"
            return
        }
        binding.patientSmsCodeInput.error = null

        if (verificationId.isBlank()) {
            binding.patientSmsCodeInput.error = "Brak danych weryfikacji. Wroc i wyslij kod ponownie."
            return
        }

        binding.patientSmsConfirm.isEnabled = false

        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                binding.patientSmsConfirm.isEnabled = true
                if (task.isSuccessful) {
                    findNavController().navigate(
                        R.id.patientHomeFragment,
                        null,
                        PhoneAuthUi.navOptionsPopToRoleSelect()
                    )
                } else {
                    binding.patientSmsCodeInput.error = "Nieprawidlowy kod"
                }
            }
    }
}

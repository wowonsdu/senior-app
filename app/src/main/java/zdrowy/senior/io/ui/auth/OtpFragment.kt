package zdrowy.senior.io.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import zdrowy.senior.io.R
import zdrowy.senior.io.databinding.FragmentOtpBinding
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import zdrowy.senior.io.domain.session.SetSessionUseCase
import zdrowy.senior.io.domain.session.UserRole
import zdrowy.senior.io.domain.session.UserSession

class OtpFragment : Fragment(), KoinComponent {
    private val viewModel: OtpViewModel by viewModels()
    private var binding: FragmentOtpBinding? = null

    private var verificationId: String = ""
    private var inviteId: String? = null
    private var role: UserRole = UserRole.PATIENT
    private var phone: String? = null
    private val setSessionUseCase: SetSessionUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verificationId = arguments?.getString(ARG_VERIFICATION_ID).orEmpty()
        inviteId = arguments?.getString(ARG_INVITE_ID)
        val roleName = arguments?.getString(ARG_ROLE) ?: UserRole.PATIENT.name
        role = UserRole.valueOf(roleName)
        phone = arguments?.getString(ARG_PHONE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOtpBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewBinding = binding ?: return

        viewBinding.otpPhoneText.text = phone ?: ""
        viewBinding.otpInput.addTextChangedListener(clearErrorWatcher())
        viewBinding.verifyButton.setOnClickListener { onVerifyClicked() }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            viewBinding.progressBar.visibility = if (state.loading) View.VISIBLE else View.GONE
            viewBinding.errorText.visibility = if (state.errorMessage.isNullOrBlank()) View.GONE else View.VISIBLE
            viewBinding.errorText.text = state.errorMessage
            viewBinding.verifyButton.isEnabled = !state.loading
        }

        viewModel.sessionEvent.observe(viewLifecycleOwner) { event ->
            val session = event.getContentIfNotHandled() ?: return@observe
            setSessionUseCase.execute(
                UserSession(uid = session.uid, role = role, phoneE164 = phone)
            )
            findNavController().navigate(R.id.action_otp_to_launcher)
        }
    }

    private fun onVerifyClicked() {
        val viewBinding = binding ?: return
        val code = viewBinding.otpInput.text.toString().trim()
        if (verificationId.isBlank()) {
            showError(getString(R.string.otp_error_missing_verification))
            return
        }
        if (code.length < 4) {
            showError(getString(R.string.otp_error_invalid_code))
            return
        }
        viewModel.verify(verificationId, code, inviteId)
    }

    private fun showError(message: String) {
        binding?.errorText?.visibility = View.VISIBLE
        binding?.errorText?.text = message
    }

    private fun clearErrorWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding?.errorText?.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    companion object {
        const val ARG_VERIFICATION_ID = "arg_verification_id"
        const val ARG_ROLE = "arg_role"
        const val ARG_PHONE = "arg_phone"
        const val ARG_INVITE_ID = "arg_invite_id"
    }
}

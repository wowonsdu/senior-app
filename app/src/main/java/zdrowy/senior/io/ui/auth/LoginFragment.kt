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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import zdrowy.senior.io.R
import zdrowy.senior.io.databinding.FragmentLoginBinding
import zdrowy.senior.io.domain.session.SetSessionUseCase
import zdrowy.senior.io.domain.session.UserRole
import zdrowy.senior.io.domain.session.UserSession

class LoginFragment : Fragment(), KoinComponent {
    private val viewModel: LoginViewModel by viewModels()
    private val setSessionUseCase: SetSessionUseCase by inject()
    private var binding: FragmentLoginBinding? = null

    private var selectedRole: AuthRole = AuthRole.PATIENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roleName = arguments?.getString(ARG_ROLE) ?: AuthRole.PATIENT.name
        selectedRole = AuthRole.valueOf(roleName)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewBinding = binding ?: return

        viewBinding.loginRoleBadge.text = if (selectedRole == AuthRole.PATIENT) {
            getString(R.string.login_role_patient)
        } else {
            getString(R.string.login_role_caregiver)
        }
        viewBinding.loginRoleBadge.setBackgroundResource(
            if (selectedRole == AuthRole.PATIENT) {
                R.drawable.bg_role_badge_patient
            } else {
                R.drawable.bg_role_badge_caregiver
            }
        )

        viewBinding.loginBackButton.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_launcher)
        }

        viewBinding.phoneInput.addTextChangedListener(clearErrorWatcher())
        viewBinding.codeInput.addTextChangedListener(clearErrorWatcher())

        viewBinding.continueButton.setOnClickListener {
            onContinueClicked()
        }

        viewBinding.mockPatientButton.setOnClickListener {
            setSessionUseCase.execute(
                UserSession(uid = "test_patient", role = UserRole.PATIENT, phoneE164 = "+48111111111")
            )
            findNavController().navigate(R.id.action_login_to_launcher)
        }

        viewBinding.mockCaregiverButton.setOnClickListener {
            setSessionUseCase.execute(
                UserSession(uid = "test_caregiver", role = UserRole.CAREGIVER, phoneE164 = "+48222222222")
            )
            findNavController().navigate(R.id.action_login_to_launcher)
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            viewBinding.progressBar.visibility = if (state.loading) View.VISIBLE else View.GONE
            viewBinding.errorText.visibility = if (state.errorMessage.isNullOrBlank()) View.GONE else View.VISIBLE
            viewBinding.errorText.text = state.errorMessage
            viewBinding.continueButton.isEnabled = !state.loading
        }

        viewModel.verificationEvent.observe(viewLifecycleOwner) { event ->
            val verification = event.getContentIfNotHandled() ?: return@observe
            val bundle = Bundle().apply {
                putString(OtpFragment.ARG_VERIFICATION_ID, verification.verificationId)
                putString(OtpFragment.ARG_ROLE, selectedRole.name)
                putString(OtpFragment.ARG_PHONE, normalizePhone(viewBinding.phoneInput.text.toString()))
                putString(OtpFragment.ARG_INVITE_ID, verification.inviteId)
            }
            findNavController().navigate(R.id.action_login_to_otp, bundle)
        }
    }

    private fun onContinueClicked() {
        val viewBinding = binding ?: return
        val rawPhone = viewBinding.phoneInput.text.toString()
        val inviteCode = viewBinding.codeInput.text.toString().trim()

        if (rawPhone.isBlank()) {
            showError(getString(R.string.login_error_phone_required))
            return
        }

        if (inviteCode.isNotBlank() && rawPhone.isBlank()) {
            showError(getString(R.string.login_error_code_requires_phone))
            return
        }

        val phoneE164 = normalizePhone(rawPhone)
        if (!phoneE164.startsWith("+") || phoneE164.length < 10) {
            showError(getString(R.string.login_error_phone_invalid))
            return
        }

        viewModel.sendOtp(phoneE164, inviteCode, requireActivity())
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

    private fun normalizePhone(rawPhone: String): String {
        val digits = rawPhone.filter { it.isDigit() || it == '+' }
        if (digits.startsWith("+")) {
            return digits
        }
        val onlyDigits = rawPhone.filter { it.isDigit() }
        return if (onlyDigits.length == 9) {
            "+48$onlyDigits"
        } else {
            onlyDigits
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    companion object {
        const val ARG_ROLE = "arg_role"
    }
}

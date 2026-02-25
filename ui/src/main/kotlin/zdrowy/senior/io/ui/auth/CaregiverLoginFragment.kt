package zdrowy.senior.io.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.android.ext.android.inject
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentCaregiverLoginBinding
import zdrowy.senior.io.domain.user.EnsureUserProfileUseCase
import zdrowy.senior.io.domain.user.SetCurrentUserRoleUseCase
import zdrowy.senior.io.domain.user.UserRole
import java.util.concurrent.TimeUnit

class CaregiverLoginFragment : Fragment() {
    private var _binding: FragmentCaregiverLoginBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val ensureUserProfile: EnsureUserProfileUseCase by inject()
    private val setCurrentUserRole: SetCurrentUserRoleUseCase by inject()
    private val disposables = CompositeDisposable()

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
            startPhoneVerification()
        }
        return binding.root
    }

    override fun onDestroyView() {
        disposables.clear()
        _binding = null
        super.onDestroyView()
    }

    private fun startPhoneVerification() {
        val phoneRaw = binding.caregiverLoginPhoneInput.editText?.text?.toString()?.trim().orEmpty()
        val phoneE164 = normalizePhoneNumberPl(phoneRaw)
        if (phoneE164 == null) {
            binding.caregiverLoginPhoneInput.error =
                if (phoneRaw.isBlank()) "Pole wymagane" else "Podaj numer w formacie +48..."
            return
        }
        binding.caregiverLoginPhoneInput.error = null

        binding.caregiverLoginContinue.isEnabled = false

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        binding.caregiverLoginContinue.isEnabled = true
                        if (task.isSuccessful) {
                            handleLoginSuccess()
                        } else {
                            binding.caregiverLoginPhoneInput.error = "Nie udalo sie zalogowac"
                        }
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                binding.caregiverLoginContinue.isEnabled = true
                binding.caregiverLoginPhoneInput.error = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Nieprawidlowy numer telefonu"
                    is FirebaseAuthMissingActivityForRecaptchaException -> "Brak Activity dla weryfikacji"
                    else -> e.localizedMessage ?: "Nie udalo sie wyslac kodu"
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                binding.caregiverLoginContinue.isEnabled = true
                findNavController().navigate(
                    R.id.action_caregiverLogin_to_caregiverSmsVerify,
                    bundleOf(
                        PhoneAuthUi.ARG_VERIFICATION_ID to verificationId,
                        PhoneAuthUi.ARG_PHONE_E164 to phoneE164
                    )
                )
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneE164)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun handleLoginSuccess() {
        disposables.add(
            ensureUserProfile(UserRole.CAREGIVER)
                .andThen(setCurrentUserRole(UserRole.CAREGIVER))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    findNavController().navigate(
                        R.id.caregiverHomeFragment,
                        null,
                        PhoneAuthUi.navOptionsPopToRoleSelect()
                    )
                }, { error ->
                    auth.signOut()
                    binding.caregiverLoginPhoneInput.error =
                        "Nie mozna ustawic roli konta: ${error.message ?: "blad"}"
                })
        )
    }
}

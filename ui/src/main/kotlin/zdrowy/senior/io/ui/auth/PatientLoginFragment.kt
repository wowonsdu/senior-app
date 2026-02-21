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
import zdrowy.senior.io.ui.databinding.FragmentPatientLoginBinding
import zdrowy.senior.io.domain.carelink.ConsumeCareLinkCodeUseCase
import zdrowy.senior.io.domain.user.EnsureUserProfileUseCase
import zdrowy.senior.io.domain.user.SetCurrentUserRoleUseCase
import zdrowy.senior.io.domain.user.UserRole
import java.util.concurrent.TimeUnit

class PatientLoginFragment : Fragment() {
    private var _binding: FragmentPatientLoginBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private val ensureUserProfile: EnsureUserProfileUseCase by inject()
    private val setCurrentUserRole: SetCurrentUserRoleUseCase by inject()
    private val consumeCareLinkCode: ConsumeCareLinkCodeUseCase by inject()
    private val disposables = CompositeDisposable()

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
            startPhoneVerification()
        }
        binding.patientLoginContinue.setOnLongClickListener {
            findNavController().navigate(R.id.action_patientLogin_to_devHub)
            true
        }
        return binding.root
    }

    override fun onDestroyView() {
        disposables.clear()
        _binding = null
        super.onDestroyView()
    }

    private fun startPhoneVerification() {
        val pendingCode = readCareLinkCodeOrNull() ?: return
        val phoneRaw = binding.patientLoginPhoneInput.editText?.text?.toString()?.trim().orEmpty()
        val phoneE164 = normalizePhoneNumberPl(phoneRaw)
        if (phoneE164 == null) {
            binding.patientLoginPhoneInput.error =
                if (phoneRaw.isBlank()) "Pole wymagane" else "Podaj numer w formacie +48..."
            return
        }
        binding.patientLoginPhoneInput.error = null

        binding.patientLoginContinue.isEnabled = false

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        binding.patientLoginContinue.isEnabled = true
                        if (task.isSuccessful) {
                            disposables.add(
                                ensureUserProfile(UserRole.PATIENT)
                                    .andThen(setCurrentUserRole(UserRole.PATIENT))
                                    .andThen(
                                        if (pendingCode.isNotBlank()) {
                                            consumeCareLinkCode(pendingCode).ignoreElement()
                                        } else {
                                            io.reactivex.rxjava3.core.Completable.complete()
                                        }
                                    )
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        findNavController().navigate(
                                            R.id.patientHomeFragment,
                                            null,
                                            PhoneAuthUi.navOptionsPopToRoleSelect()
                                        )
                                    }, { error ->
                                        auth.signOut()
                                        binding.patientLoginPhoneInput.error =
                                            "Nie mozna ustawic roli konta: ${error.message ?: "blad"}"
                                    })
                            )
                        } else {
                            binding.patientLoginPhoneInput.error = "Nie udalo sie zalogowac"
                        }
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                binding.patientLoginContinue.isEnabled = true

                binding.patientLoginPhoneInput.error = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Nieprawidlowy numer telefonu"
                    is FirebaseAuthMissingActivityForRecaptchaException -> "Brak Activity dla weryfikacji"
                    else -> e.localizedMessage ?: "Nie udalo sie wyslac kodu"
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                binding.patientLoginContinue.isEnabled = true
                resendToken = token
                findNavController().navigate(
                    R.id.action_patientLogin_to_patientSmsVerify,
                    bundleOf(
                        PhoneAuthUi.ARG_VERIFICATION_ID to verificationId,
                        PhoneAuthUi.ARG_PHONE_E164 to phoneE164,
                        PhoneAuthUi.ARG_PENDING_CARE_LINK_CODE to pendingCode
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

    private fun readCareLinkCodeOrNull(): String? {
        val raw = binding.patientLoginAccessCode.text?.toString()?.trim().orEmpty()
        if (raw.isBlank()) {
            binding.patientLoginCodeInput.error = null
            return ""
        }
        val digits = raw.filter { it.isDigit() }
        return if (digits.length == 6) {
            binding.patientLoginCodeInput.error = null
            digits
        } else {
            binding.patientLoginCodeInput.error = "Kod musi miec 6 cyfr"
            null
        }
    }
}

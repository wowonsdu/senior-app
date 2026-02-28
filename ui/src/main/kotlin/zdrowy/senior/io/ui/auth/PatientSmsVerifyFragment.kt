package zdrowy.senior.io.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.android.ext.android.inject
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientSmsVerifyBinding
import zdrowy.senior.io.domain.user.EnsureManagedPatientContextUseCase
import zdrowy.senior.io.domain.user.EnsureUserProfileUseCase
import zdrowy.senior.io.domain.user.SetCurrentUserRoleUseCase
import zdrowy.senior.io.domain.user.UserRole

class PatientSmsVerifyFragment : Fragment() {
    private var _binding: FragmentPatientSmsVerifyBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val ensureUserProfile: EnsureUserProfileUseCase by inject()
    private val setCurrentUserRole: SetCurrentUserRoleUseCase by inject()
    private val ensureManagedPatientContext: EnsureManagedPatientContextUseCase by inject()
    private val disposables = CompositeDisposable()

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
        disposables.clear()
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
                if (task.isSuccessful) {
                    disposables.add(
                        ensureUserProfile(UserRole.PATIENT)
                            .andThen(setCurrentUserRole(UserRole.PATIENT))
                            .andThen(ensureManagedPatientContext())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doFinally { binding.patientSmsConfirm.isEnabled = true }
                            .subscribe({
                                findNavController().navigate(
                                    R.id.caregiverLinkFragment,
                                    bundleOf(PhoneAuthUi.ARG_LINK_FROM_AUTH to true),
                                    PhoneAuthUi.navOptionsPopToRoleSelect()
                                )
                            }, { error ->
                                auth.signOut()
                                binding.patientSmsCodeInput.error =
                                    "Nie mozna ustawic roli konta: ${error.message ?: "blad"}"
                            })
                    )
                } else {
                    binding.patientSmsConfirm.isEnabled = true
                    binding.patientSmsCodeInput.error = "Nieprawidlowy kod"
                }
            }
    }
}

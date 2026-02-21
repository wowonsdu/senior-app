package zdrowy.senior.io.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.android.ext.android.inject
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentCaregiverSmsVerifyBinding
import zdrowy.senior.io.domain.user.EnsureUserProfileUseCase
import zdrowy.senior.io.domain.user.UserRole

class CaregiverSmsVerifyFragment : Fragment() {
    private var _binding: FragmentCaregiverSmsVerifyBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val ensureUserProfile: EnsureUserProfileUseCase by inject()
    private val disposables = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaregiverSmsVerifyBinding.inflate(inflater, container, false)
        val phone = arguments?.getString(PhoneAuthUi.ARG_PHONE_E164).orEmpty()
        val verificationId = arguments?.getString(PhoneAuthUi.ARG_VERIFICATION_ID).orEmpty()
        binding.caregiverSmsPhone.text = phone.ifBlank { getString(R.string.sms_verify_phone) }

        binding.caregiverSmsBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.caregiverSmsConfirm.setOnClickListener {
            confirmCode(verificationId)
        }
        binding.caregiverSmsResend.setOnClickListener {
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
        val code = binding.caregiverSmsCodeInput.editText?.text?.toString()?.trim().orEmpty()
        if (code.isBlank()) {
            binding.caregiverSmsCodeInput.error = "Pole wymagane"
            return
        }
        binding.caregiverSmsCodeInput.error = null

        if (verificationId.isBlank()) {
            binding.caregiverSmsCodeInput.error = "Brak danych weryfikacji. Wroc i wyslij kod ponownie."
            return
        }

        binding.caregiverSmsConfirm.isEnabled = false

        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    disposables.add(
                        ensureUserProfile(UserRole.CAREGIVER)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doFinally { binding.caregiverSmsConfirm.isEnabled = true }
                            .subscribe({
                                findNavController().navigate(
                                    R.id.caregiverLinkFragment,
                                    null,
                                    PhoneAuthUi.navOptionsPopToRoleSelect()
                                )
                            }, { error ->
                                auth.signOut()
                                binding.caregiverSmsCodeInput.error =
                                    "Nie mozna ustawic roli konta: ${error.message ?: "blad"}"
                            })
                    )
                } else {
                    binding.caregiverSmsConfirm.isEnabled = true
                    binding.caregiverSmsCodeInput.error = "Nieprawidlowy kod"
                }
            }
    }
}

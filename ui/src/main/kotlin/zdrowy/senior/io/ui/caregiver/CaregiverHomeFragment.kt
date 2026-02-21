package zdrowy.senior.io.ui.caregiver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.android.ext.android.inject
import zdrowy.senior.io.domain.user.ClearActivePatientUseCase
import zdrowy.senior.io.domain.user.ClearCurrentUserRoleUseCase
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentCaregiverHomeBinding

class CaregiverHomeFragment : Fragment() {
    private var _binding: FragmentCaregiverHomeBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val clearActivePatient: ClearActivePatientUseCase by inject()
    private val clearCurrentUserRole: ClearCurrentUserRoleUseCase by inject()
    private val disposables = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaregiverHomeBinding.inflate(inflater, container, false)

        binding.caregiverHomeBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.caregiverHomeLogout.setOnClickListener {
            auth.signOut()
            disposables.add(
                clearActivePatient()
                    .andThen(clearCurrentUserRole())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        findNavController().navigate(
                            R.id.startupGateFragment,
                            null,
                            androidx.navigation.NavOptions.Builder()
                                .setPopUpTo(R.id.caregiverHomeFragment, true)
                                .setLaunchSingleTop(true)
                                .build()
                        )
                    }, {
                        findNavController().navigate(
                            R.id.startupGateFragment,
                            null,
                            androidx.navigation.NavOptions.Builder()
                                .setPopUpTo(R.id.caregiverHomeFragment, true)
                                .setLaunchSingleTop(true)
                                .build()
                        )
                    })
            )
        }

        binding.caregiverHomePhone.text = auth.currentUser?.phoneNumber ?: "-"

        return binding.root
    }

    override fun onDestroyView() {
        disposables.clear()
        _binding = null
        super.onDestroyView()
    }
}

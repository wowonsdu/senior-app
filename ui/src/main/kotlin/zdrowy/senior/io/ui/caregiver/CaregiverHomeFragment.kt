package zdrowy.senior.io.ui.caregiver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pagerAdapter = CaregiverHomePagerAdapter(this)
        binding.caregiverHomePager.adapter = pagerAdapter
        TabLayoutMediator(binding.caregiverHomeTabs, binding.caregiverHomePager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.caregiver_tab_dashboard)
                1 -> getString(R.string.caregiver_tab_dependents)
                else -> getString(R.string.caregiver_tab_visits)
            }
        }.attach()

        binding.caregiverHomeToolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.caregiver_home_logout) {
                logout()
                true
            } else {
                false
            }
        }
    }

    private fun logout() {
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

    override fun onDestroyView() {
        disposables.clear()
        _binding = null
        super.onDestroyView()
    }
}

package zdrowy.senior.io.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.viewmodel.ext.android.viewModel
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentStartupGateBinding

class StartupGateFragment : Fragment() {
    private var _binding: FragmentStartupGateBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val viewModel: StartupGateViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartupGateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (auth.currentUser == null) {
            navigateToRoleSelect()
            return
        }
        viewModel.start()
        viewModel.navTarget.observe(viewLifecycleOwner) { target ->
            when (target) {
                StartupGateNavTarget.PatientHome -> navigateToPatientHome()
                StartupGateNavTarget.CaregiverHome -> navigateToCaregiverHome()
                StartupGateNavTarget.RoleSelect -> {
                    auth.signOut()
                    navigateToRoleSelect()
                }
                null -> Unit
            }
            if (target != null) viewModel.onNavigationHandled()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun navigateToPatientHome() {
        findNavController().navigate(
            R.id.patientHomeFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.startupGateFragment, true)
                .setLaunchSingleTop(true)
                .build()
        )
    }

    private fun navigateToCaregiverHome() {
        findNavController().navigate(
            R.id.caregiverHomeFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.startupGateFragment, true)
                .setLaunchSingleTop(true)
                .build()
        )
    }

    private fun navigateToRoleSelect() {
        findNavController().navigate(
            R.id.roleSelectFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.startupGateFragment, true)
                .setLaunchSingleTop(true)
                .build()
        )
    }
}

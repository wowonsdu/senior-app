package zdrowy.senior.io.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentRoleSelectBinding

class RoleSelectFragment : Fragment() {
    private var _binding: FragmentRoleSelectBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoleSelectBinding.inflate(inflater, container, false)

        binding.roleSelectPatientCard.setOnClickListener {
            if (auth.currentUser != null) {
                findNavController().navigate(
                    R.id.patientHomeFragment,
                    null,
                    PhoneAuthUi.navOptionsPopToRoleSelect()
                )
            } else {
                findNavController().navigate(R.id.action_roleSelect_to_patientLogin)
            }
        }

        binding.roleSelectCaregiverCard.setOnClickListener {
            if (auth.currentUser != null) {
                findNavController().navigate(
                    R.id.patientHomeFragment,
                    null,
                    PhoneAuthUi.navOptionsPopToRoleSelect()
                )
            } else {
                findNavController().navigate(R.id.action_roleSelect_to_caregiverLogin)
            }
        }

        binding.roleSelectDevSkip.setOnClickListener {
            findNavController().navigate(R.id.action_roleSelect_to_devHub)
        }

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

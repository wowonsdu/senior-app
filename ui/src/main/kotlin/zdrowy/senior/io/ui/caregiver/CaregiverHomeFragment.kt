package zdrowy.senior.io.ui.caregiver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentCaregiverHomeBinding

class CaregiverHomeFragment : Fragment() {
    private var _binding: FragmentCaregiverHomeBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

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
            findNavController().navigate(
                R.id.roleSelectFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.roleSelectFragment, false)
                    .setLaunchSingleTop(true)
                    .build()
            )
        }

        binding.caregiverHomePhone.text = auth.currentUser?.phoneNumber ?: "-"

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}


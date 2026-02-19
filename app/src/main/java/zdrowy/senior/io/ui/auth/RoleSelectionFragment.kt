package zdrowy.senior.io.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import zdrowy.senior.io.R
import zdrowy.senior.io.databinding.FragmentRoleSelectionBinding

class RoleSelectionFragment : Fragment() {
    private val viewModel: RoleSelectionViewModel by viewModels()
    private var binding: FragmentRoleSelectionBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRoleSelectionBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.apply {
            rolePatientCard.setOnClickListener { onRoleSelected(AuthRole.PATIENT) }
            roleCaregiverCard.setOnClickListener { onRoleSelected(AuthRole.CAREGIVER) }
        }
    }

    private fun onRoleSelected(role: AuthRole) {
        viewModel.selectRole(role)
        val bundle = Bundle().apply {
            putString(LoginFragment.ARG_ROLE, role.name)
        }
        findNavController().navigate(R.id.action_roleSelection_to_login, bundle)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}

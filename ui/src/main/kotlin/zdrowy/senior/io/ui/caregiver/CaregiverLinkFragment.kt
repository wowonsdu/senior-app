package zdrowy.senior.io.ui.caregiver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.auth.PhoneAuthUi
import zdrowy.senior.io.ui.databinding.FragmentCaregiverLinkBinding

class CaregiverLinkFragment : Fragment() {
    private var _binding: FragmentCaregiverLinkBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CaregiverLinkViewModel by viewModel()
    private var fromAuthFlow: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaregiverLinkBinding.inflate(inflater, container, false)
        fromAuthFlow = arguments?.getBoolean(PhoneAuthUi.ARG_LINK_FROM_AUTH) == true
        binding.caregiverLinkToolbar.setNavigationOnClickListener {
            handleCancel()
        }
        binding.caregiverLinkCancel.setOnClickListener {
            handleCancel()
        }
        binding.caregiverLinkConnect.setOnClickListener {
            val code = binding.caregiverLinkCode.text?.toString()?.trim().orEmpty()
            viewModel.consumeCode(code)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.start(fromAuthFlow)
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            binding.caregiverLinkCodeInput.error = message
        }
        viewModel.navTarget.observe(viewLifecycleOwner) { target ->
            when (target) {
                CareLinkNavTarget.PATIENT_HOME -> {
                    findNavController().navigate(
                        R.id.patientHomeFragment,
                        null,
                        PhoneAuthUi.navOptionsPopToRoleSelect()
                    )
                }
                CareLinkNavTarget.POP_BACK -> findNavController().popBackStack()
                null -> Unit
            }
            if (target != null) viewModel.onNavigationHandled()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun handleCancel() {
        if (fromAuthFlow) {
            findNavController().navigate(
                R.id.patientHomeFragment,
                null,
                PhoneAuthUi.navOptionsPopToRoleSelect()
            )
        } else {
            findNavController().popBackStack()
        }
    }
}

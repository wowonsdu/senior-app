package zdrowy.senior.io.ui.caregiver

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaregiverLinkBinding.inflate(inflater, container, false)
        binding.caregiverLinkToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.caregiverLinkCancel.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.caregiverLinkConnect.setOnClickListener {
            val code = binding.caregiverLinkCode.text?.toString()?.trim().orEmpty()
            viewModel.consumeCode(code)
        }
        binding.caregiverLinkGenerate.setOnClickListener {
            viewModel.generateCode()
        }
        binding.caregiverLinkCopy.setOnClickListener {
            copyCode()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.start()
        viewModel.generatedCode.observe(viewLifecycleOwner) { code ->
            binding.caregiverLinkCodeValue.text = code
        }
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

    private fun copyCode() {
        val code = binding.caregiverLinkCodeValue.text?.toString().orEmpty()
        if (code.isBlank()) return
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("careLinkCode", code))
    }
}

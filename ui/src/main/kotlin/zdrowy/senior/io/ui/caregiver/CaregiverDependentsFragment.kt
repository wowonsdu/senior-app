package zdrowy.senior.io.ui.caregiver

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.common.GridSpacingItemDecoration
import zdrowy.senior.io.ui.databinding.FragmentCaregiverDependentsBinding

class CaregiverDependentsFragment : Fragment() {
    private var _binding: FragmentCaregiverDependentsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CaregiverDependentsViewModel by viewModel()
    private val adapter = CaregiverDependentsAdapter(
        onSelect = { item ->
            viewModel.selectDependent(item.uid)
        },
        onToggleReminder = { item, enabled ->
            viewModel.setMedicationReminderEnabled(item.uid, enabled)
        },
        onLinkAccount = { item ->
            viewModel.generatePatientLinkCode(item.uid)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaregiverDependentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.caregiverDependentsList.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.caregiverDependentsList.addItemDecoration(
            GridSpacingItemDecoration(
                spanCount = 2,
                gapPx = resources.getDimensionPixelSize(R.dimen.grid_item_gap),
                includeEdge = false
            )
        )
        binding.caregiverDependentsList.adapter = adapter
        binding.caregiverDependentsAdd.setOnClickListener {
            requireParentFragment().findNavController().navigate(R.id.caregiverAddDependentFragment)
        }

        viewModel.start()
        viewModel.dependents.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
        viewModel.navTarget.observe(viewLifecycleOwner) { target ->
            if (target != null) {
                requireParentFragment().findNavController().navigate(R.id.patientHomeFragment)
                viewModel.onNavigationHandled()
            }
        }
        viewModel.linkCodeToCopy.observe(viewLifecycleOwner) { code ->
            if (code.isNullOrBlank()) return@observe
            val clipboard = requireContext()
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("careLinkCode", code))
            Toast.makeText(requireContext(), "Skopiowano kod: $code", Toast.LENGTH_SHORT).show()
            viewModel.onLinkCodeHandled()
        }
        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message.isNullOrBlank()) return@observe
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            viewModel.onMessageHandled()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

package zdrowy.senior.io.ui.caregiver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import zdrowy.senior.io.ui.R
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
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

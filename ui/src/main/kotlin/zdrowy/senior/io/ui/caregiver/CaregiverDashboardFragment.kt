package zdrowy.senior.io.ui.caregiver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentCaregiverDashboardBinding

class CaregiverDashboardFragment : Fragment() {
    private var _binding: FragmentCaregiverDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CaregiverDashboardViewModel by viewModel()
    private val adapter = CaregiverMeasurementNotificationsAdapter { item ->
        viewModel.markRead(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaregiverDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.caregiverDashboardList.layoutManager = LinearLayoutManager(requireContext())
        binding.caregiverDashboardList.adapter = adapter
        binding.caregiverDashboardMarkAll.setOnClickListener {
            viewModel.markAllRead()
        }
        binding.caregiverDashboardTabs.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                binding.caregiverDashboardTabNew.id -> viewModel.setTab(CaregiverDashboardTab.NEW)
                binding.caregiverDashboardTabRead.id -> viewModel.setTab(CaregiverDashboardTab.READ)
            }
        }

        viewModel.start()
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            val selectedId = when (state.selectedTab) {
                CaregiverDashboardTab.NEW -> binding.caregiverDashboardTabNew.id
                CaregiverDashboardTab.READ -> binding.caregiverDashboardTabRead.id
            }
            if (binding.caregiverDashboardTabs.checkedButtonId != selectedId) {
                binding.caregiverDashboardTabs.check(selectedId)
            }

            val visibleItems = state.visibleItems()
            adapter.submitList(visibleItems)

            binding.caregiverDashboardCount.text = state.newCount.toString()
            binding.caregiverDashboardCountChip.visibility = if (state.newCount > 0) View.VISIBLE else View.GONE

            val showEmpty = state.showMissingPatient || visibleItems.isEmpty()
            binding.caregiverDashboardEmpty.visibility = if (showEmpty) View.VISIBLE else View.GONE
            binding.caregiverDashboardList.visibility = if (showEmpty) View.GONE else View.VISIBLE
            binding.caregiverDashboardEmpty.text = when {
                state.showMissingPatient -> getString(R.string.caregiver_dashboard_empty_missing_patient)
                state.selectedTab == CaregiverDashboardTab.NEW -> getString(R.string.caregiver_dashboard_empty_new)
                else -> getString(R.string.caregiver_dashboard_empty_read)
            }

            val showContent = !state.showMissingPatient
            binding.caregiverDashboardHeader.visibility = if (showContent) View.VISIBLE else View.GONE
            binding.caregiverDashboardTabs.visibility = if (showContent) View.VISIBLE else View.GONE

            binding.caregiverDashboardMarkAll.visibility = if (
                state.selectedTab == CaregiverDashboardTab.NEW && state.newCount > 0
            ) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

package zdrowy.senior.io.ui.caregiver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.common.VerticalSpacingItemDecoration
import zdrowy.senior.io.ui.databinding.FragmentCaregiverVisitsBinding
import zdrowy.senior.io.ui.caregiver.model.CaregiverVisitFilter

class CaregiverVisitsFragment : Fragment() {
    private var _binding: FragmentCaregiverVisitsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CaregiverVisitsViewModel by viewModel()
    private val adapter = CaregiverVisitsAdapter(
        onComplete = { item ->
            viewModel.markCompleted(item.id)
        },
        onEdit = { item ->
            viewModel.onEditClicked(item.id)
        },
        onDelete = { item ->
            confirmDelete(item.id)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaregiverVisitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupList()
        setupFilters()
        binding.caregiverVisitsAdd.setOnClickListener {
            viewModel.onAddClicked()
        }

        viewModel.start()
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            renderFilter(state.selectedFilter)
            adapter.submitList(state.items)
            val showEmpty = state.isEmpty
            binding.caregiverVisitsEmpty.visibility = if (showEmpty) View.VISIBLE else View.GONE
            binding.caregiverVisitsList.visibility = if (showEmpty) View.GONE else View.VISIBLE
            binding.caregiverVisitsEmpty.text = when (state.selectedFilter) {
                CaregiverVisitFilter.UPCOMING -> getString(R.string.caregiver_visits_empty_upcoming)
                CaregiverVisitFilter.ALL -> getString(R.string.caregiver_visits_empty_all)
                CaregiverVisitFilter.COMPLETED -> getString(R.string.caregiver_visits_empty_completed)
            }
        }
        viewModel.navTarget.observe(viewLifecycleOwner) {
            viewModel.onNavigationHandled()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupList() {
        binding.caregiverVisitsList.layoutManager = LinearLayoutManager(requireContext())
        binding.caregiverVisitsList.addItemDecoration(
            VerticalSpacingItemDecoration(
                gapPx = resources.getDimensionPixelSize(R.dimen.list_item_gap)
            )
        )
        binding.caregiverVisitsList.adapter = adapter
    }

    private fun setupFilters() {
        binding.caregiverVisitsFilters.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                binding.caregiverVisitsFilterUpcoming.id -> viewModel.setFilter(CaregiverVisitFilter.UPCOMING)
                binding.caregiverVisitsFilterAll.id -> viewModel.setFilter(CaregiverVisitFilter.ALL)
                binding.caregiverVisitsFilterCompleted.id -> viewModel.setFilter(CaregiverVisitFilter.COMPLETED)
            }
        }
    }

    private fun renderFilter(filter: CaregiverVisitFilter) {
        val buttonId = when (filter) {
            CaregiverVisitFilter.UPCOMING -> binding.caregiverVisitsFilterUpcoming.id
            CaregiverVisitFilter.ALL -> binding.caregiverVisitsFilterAll.id
            CaregiverVisitFilter.COMPLETED -> binding.caregiverVisitsFilterCompleted.id
        }
        if (binding.caregiverVisitsFilters.checkedButtonId != buttonId) {
            binding.caregiverVisitsFilters.check(buttonId)
        }
    }

    private fun confirmDelete(visitId: String) {
        MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setTitle(R.string.caregiver_visits_delete_title)
            .setMessage(R.string.caregiver_visits_delete_message)
            .setNegativeButton(R.string.caregiver_add_dependent_cancel, null)
            .setPositiveButton(R.string.caregiver_visits_action_delete) { _, _ ->
                viewModel.removeVisit(visitId)
            }
            .show()
    }
}

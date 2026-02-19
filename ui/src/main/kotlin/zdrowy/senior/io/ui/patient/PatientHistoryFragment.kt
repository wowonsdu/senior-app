package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.get
import zdrowy.senior.io.ui.databinding.FragmentPatientHistoryBinding

class PatientHistoryFragment : Fragment() {
    private var _binding: FragmentPatientHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PatientHistoryViewModel> {
        PatientHistoryViewModel.Factory(get(), get(), get())
    }
    private val adapter = PatientMeasurementsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientHistoryBinding.inflate(inflater, container, false)
        binding.patientHistoryToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientHistoryList.layoutManager = LinearLayoutManager(requireContext())
        binding.patientHistoryList.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.load()
        viewModel.measurements.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

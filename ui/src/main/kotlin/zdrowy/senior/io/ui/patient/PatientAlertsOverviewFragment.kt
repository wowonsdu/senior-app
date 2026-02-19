package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import org.koin.android.ext.android.get
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientAlertsOverviewBinding

class PatientAlertsOverviewFragment : Fragment() {
    private var _binding: FragmentPatientAlertsOverviewBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PatientAlertsViewModel> {
        PatientAlertsViewModel.Factory(get())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientAlertsOverviewBinding.inflate(inflater, container, false)
        binding.patientAlertsOverviewToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.patientAlertsOverviewContent.alertsOverviewSave.setOnClickListener {
            findNavController().navigate(R.id.action_alertsOverview_to_alertsConfig)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.load()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

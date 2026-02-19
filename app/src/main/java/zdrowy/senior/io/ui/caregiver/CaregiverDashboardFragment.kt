package zdrowy.senior.io.ui.caregiver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import zdrowy.senior.io.R
import zdrowy.senior.io.databinding.FragmentCaregiverDashboardBinding
import zdrowy.senior.io.domain.session.GetActivePatientUseCase

class CaregiverDashboardFragment : Fragment(), KoinComponent {
    private var binding: FragmentCaregiverDashboardBinding? = null
    private val patientStore: PatientStore by inject()
    private val getActivePatientUseCase: GetActivePatientUseCase by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCaregiverDashboardBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activeId = getActivePatientUseCase.execute()
        val activePatient = patientStore.getPatients().firstOrNull { it.id == activeId }
        binding?.activePatientText?.text = activePatient?.name ?: getString(R.string.patient_none_selected)

        binding?.selectPatientButton?.setOnClickListener {
            findNavController().navigate(R.id.action_caregiverDashboard_to_patientSelection)
        }

        binding?.openHomeButton?.setOnClickListener {
            findNavController().navigate(R.id.action_caregiverDashboard_to_home)
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}

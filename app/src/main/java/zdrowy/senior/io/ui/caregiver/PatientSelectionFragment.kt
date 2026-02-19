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
import zdrowy.senior.io.databinding.FragmentPatientSelectionBinding
import zdrowy.senior.io.domain.session.SetActivePatientUseCase

class PatientSelectionFragment : Fragment(), KoinComponent {
    private var binding: FragmentPatientSelectionBinding? = null
    private val patientStore: PatientStore by inject()
    private val setActivePatientUseCase: SetActivePatientUseCase by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPatientSelectionBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refreshList()

        binding?.addPatientButton?.setOnClickListener {
            val name = binding?.patientNameInput?.text?.toString()?.trim().orEmpty()
            val phone = binding?.patientPhoneInput?.text?.toString()?.trim()
            if (name.isNotBlank()) {
                val item = patientStore.addPatient(name, if (phone.isNullOrBlank()) null else phone)
                setActivePatientUseCase.execute(item.id)
                refreshList()
                binding?.patientNameInput?.text?.clear()
                binding?.patientPhoneInput?.text?.clear()
                findNavController().navigate(R.id.action_patientSelection_to_caregiverDashboard)
            }
        }
    }

    private fun refreshList() {
        val list = patientStore.getPatients()
        binding?.patientsList?.text = if (list.isEmpty()) {
            getString(R.string.patient_list_empty)
        } else {
            list.joinToString(separator = "\n") { item ->
                val phone = item.phone?.let { " ($it)" } ?: ""
                "- ${item.name}$phone"
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}

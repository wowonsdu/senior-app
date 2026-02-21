package zdrowy.senior.io.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientHomeBinding
import zdrowy.senior.io.domain.user.ClearActivePatientUseCase
import zdrowy.senior.io.domain.user.ClearCurrentUserRoleUseCase
import org.koin.android.ext.android.inject

class PatientHomeFragment : Fragment() {
    private var _binding: FragmentPatientHomeBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val viewModel: PatientHomeViewModel by viewModel()
    private val clearActivePatient: ClearActivePatientUseCase by inject()
    private val clearCurrentUserRole: ClearCurrentUserRoleUseCase by inject()
    private val disposables = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientHomeBinding.inflate(inflater, container, false)
        binding.patientHomeToolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.patient_home_logout) {
                auth.signOut()
                disposables.add(
                    clearActivePatient()
                        .andThen(clearCurrentUserRole())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            findNavController().navigate(R.id.action_patientHome_to_roleSelect)
                        }, {
                            findNavController().navigate(R.id.action_patientHome_to_roleSelect)
                        })
                )
                true
            } else {
                false
            }
        }
        binding.patientHomeTileSugar.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientSugarDialog)
        }
        binding.patientHomeTileInsulin.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientInsulinDialog)
        }
        binding.patientHomeTilePressure.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientPressureDialog)
        }
        binding.patientHomeTilePulse.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientPulseDialog)
        }
        binding.patientHomeTileNotifyAgents.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientNotifyAgentsDialog)
        }
        binding.patientHomeActionHistory.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientHistory)
        }
        binding.patientHomeActionAgents.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientAgents)
        }
        binding.patientHomeActionSettings.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientSettings)
        }
        binding.patientHomeProfile.setOnClickListener {
            viewModel.onHeaderClicked()
        }
        binding.patientHomeProfileActionContainer.setOnClickListener {
            viewModel.onHeaderClicked()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.start()
        viewModel.loadRecent()
        viewModel.headerState.observe(viewLifecycleOwner) { state ->
            binding.patientHomeProfileLabel.setText(state.labelRes)
            binding.patientHomeProfileName.text = state.fullName
            binding.patientHomeProfilePhone.text = state.phone
            binding.patientHomeAvatar.text = state.avatar
            binding.patientHomeProfileActionContainer.isVisible = state.showChevron
        }
        viewModel.navTarget.observe(viewLifecycleOwner) { target ->
            if (target == PatientHomeNavTarget.CAREGIVER_LINK) {
                findNavController().navigate(R.id.action_patientHome_to_caregiverLink)
            }
            if (target != null) viewModel.onNavigationHandled()
        }
    }

    override fun onDestroyView() {
        disposables.clear()
        _binding = null
        super.onDestroyView()
    }
}

package zdrowy.senior.io.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import zdrowy.senior.io.R
import zdrowy.senior.io.domain.session.GetActivePatientUseCase
import zdrowy.senior.io.domain.session.GetSessionUseCase
import zdrowy.senior.io.domain.session.UserRole
import zdrowy.senior.io.ui.caregiver.PatientStore

class LauncherFragment : Fragment(), KoinComponent {
    private val getSessionUseCase: GetSessionUseCase by inject()
    private val getActivePatientUseCase: GetActivePatientUseCase by inject()
    private val patientStore: PatientStore by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_launcher, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.post {
            if (!isAdded) return@post
            val navController = findNavController()
            if (navController.currentDestination?.id != R.id.launcherFragment) return@post

            val session = getSessionUseCase.execute()
            if (session == null) {
                navController.navigate(R.id.action_launcher_to_roleSelection)
                return@post
            }

            when (session.role) {
                UserRole.PATIENT -> {
                    navController.navigate(R.id.action_launcher_to_home)
                }
                UserRole.CAREGIVER -> {
                    val patients = patientStore.getPatients()
                    if (patients.isEmpty()) {
                        navController.navigate(R.id.action_launcher_to_patientSelection)
                    } else {
                        val activeId = getActivePatientUseCase.execute()
                        if (activeId.isNullOrBlank()) {
                            navController.navigate(R.id.action_launcher_to_patientSelection)
                        } else {
                            navController.navigate(R.id.action_launcher_to_caregiverDashboard)
                        }
                    }
                }
            }
        }
    }
}

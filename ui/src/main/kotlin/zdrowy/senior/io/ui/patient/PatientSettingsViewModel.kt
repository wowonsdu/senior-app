package zdrowy.senior.io.ui.patient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.domain.agent.Agent
import zdrowy.senior.io.domain.agent.AgentRole
import zdrowy.senior.io.domain.carelink.RemoveCareLinkUseCase
import zdrowy.senior.io.domain.user.ClearActivePatientUseCase
import zdrowy.senior.io.domain.user.ClearCurrentUserRoleUseCase
import zdrowy.senior.io.domain.user.DeleteCurrentUserAccountUseCase
import zdrowy.senior.io.domain.user.DeleteUserDataUseCase
import zdrowy.senior.io.domain.agent.ListAgentsUseCase
import zdrowy.senior.io.domain.agent.ObserveAgentsUseCase
import zdrowy.senior.io.domain.settings.GetSettingsOverviewUseCase
import zdrowy.senior.io.domain.settings.ObserveDiseasesUseCase
import zdrowy.senior.io.domain.settings.ObserveMedicationsUseCase
import zdrowy.senior.io.domain.settings.ObservePersonalDataUseCase
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.domain.settings.SettingsOverview
import zdrowy.senior.io.domain.user.ManagedUserUidState
import zdrowy.senior.io.domain.user.ObserveManagedUserUidStateUseCase
import zdrowy.senior.io.domain.user.UserRole

class PatientSettingsViewModel(
    private val getSettingsOverview: GetSettingsOverviewUseCase,
    private val listAgents: ListAgentsUseCase,
    private val observeAgents: ObserveAgentsUseCase,
    private val observePersonalData: ObservePersonalDataUseCase,
    private val observeDiseases: ObserveDiseasesUseCase,
    private val observeMedications: ObserveMedicationsUseCase,
    private val observeManagedUserUidState: ObserveManagedUserUidStateUseCase,
    private val deleteCurrentUserAccount: DeleteCurrentUserAccountUseCase,
    private val deleteUserData: DeleteUserDataUseCase,
    private val removeCareLink: RemoveCareLinkUseCase,
    private val clearActivePatient: ClearActivePatientUseCase,
    private val clearCurrentUserRole: ClearCurrentUserRoleUseCase
) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var settingsObserved = false
    private var currentOverview: SettingsOverview? = null
    private var currentCaregivers = emptyList<Agent>()
    private var currentDoctors = emptyList<Agent>()
    private var managedRole: UserRole? = null
    private var managedPatientUid: String? = null
    private val fallbackPersonalData = PersonalData(
        firstName = "",
        lastName = "",
        pesel = "",
        phoneNumber = "",
        email = "",
        address = ""
    )

    private val _uiState = MutableLiveData<PatientSettingsUiState>()
    val uiState: LiveData<PatientSettingsUiState> = _uiState
    private val _deletePrompt = MutableLiveData<PatientSettingsDeletePrompt?>()
    val deletePrompt: LiveData<PatientSettingsDeletePrompt?> = _deletePrompt
    private val _navTarget = MutableLiveData<PatientSettingsNavTarget?>()
    val navTarget: LiveData<PatientSettingsNavTarget?> = _navTarget
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    fun load() {
        if (currentOverview == null) {
            disposables.add(
                io.reactivex.rxjava3.core.Single.zip(
                    getSettingsOverview(),
                    listAgents()
                ) { overview, agents ->
                    currentCaregivers = agents.filter { it.role == AgentRole.CAREGIVER }
                    currentDoctors = agents.filter { it.role == AgentRole.DOCTOR }
                    currentOverview = overview
                    PatientSettingsUiState.from(overview, currentCaregivers, currentDoctors)
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ state ->
                        _uiState.value = state
                    }, {
                    })
            )
        } else {
            disposables.add(
                listAgents()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ agents ->
                        currentCaregivers = agents.filter { it.role == AgentRole.CAREGIVER }
                        currentDoctors = agents.filter { it.role == AgentRole.DOCTOR }
                        val overview = currentOverview ?: return@subscribe
                        _uiState.value = PatientSettingsUiState.from(overview, currentCaregivers, currentDoctors)
                    }, {
                    })
            )
        }
    }

    fun startSettingsObservation() {
        if (settingsObserved) return
        settingsObserved = true
        disposables.add(
            observePersonalData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data ->
                    updateOverview { it.copy(personalData = data) }
                }, {
                })
        )
        disposables.add(
            observeAgents()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ agents ->
                    currentCaregivers = agents.filter { it.role == AgentRole.CAREGIVER }
                    currentDoctors = agents.filter { it.role == AgentRole.DOCTOR }
                    val overview = currentOverview ?: return@subscribe
                    _uiState.value = PatientSettingsUiState.from(overview, currentCaregivers, currentDoctors)
                }, {
                })
        )
        disposables.add(
            observeDiseases()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ diseases ->
                    updateOverview { it.copy(diseases = diseases) }
                }, {
                })
        )
        disposables.add(
            observeMedications()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ medications ->
                    updateOverview { it.copy(medications = medications) }
                }, {
                })
        )
        disposables.add(
            observeManagedUserUidState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ state ->
                    handleManagedState(state)
                }, {
                })
        )
    }

    fun onDeleteAccountClicked() {
        when (managedRole) {
            UserRole.PATIENT -> _deletePrompt.value = PatientSettingsDeletePrompt.Self
            UserRole.CAREGIVER -> {
                if (managedPatientUid.isNullOrBlank()) {
                    _message.value = "Brak wybranego podopiecznego"
                } else {
                    _deletePrompt.value = PatientSettingsDeletePrompt.Caregiver
                }
            }
            else -> _message.value = "Brak wybranego podopiecznego"
        }
    }

    fun confirmDeleteSelf() {
        disposables.add(
            deleteCurrentUserAccount()
                .andThen(clearActivePatient().onErrorComplete())
                .andThen(clearCurrentUserRole().onErrorComplete())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    auth.signOut()
                    _navTarget.value = PatientSettingsNavTarget.STARTUP_GATE
                }, { error ->
                    _message.value = error.message ?: "Nie udalo sie usunac konta"
                })
        )
    }

    fun confirmDeletePatient() {
        val patientUid = managedPatientUid.orEmpty()
        if (patientUid.isBlank()) {
            _message.value = "Brak wybranego podopiecznego"
            return
        }
        disposables.add(
            deleteUserData(patientUid)
                .andThen(clearActivePatient().onErrorComplete())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _navTarget.value = PatientSettingsNavTarget.CAREGIVER_HOME
                }, { error ->
                    _message.value = error.message ?: "Nie udalo sie usunac konta pacjenta"
                })
        )
    }

    fun confirmStopCaregiving() {
        val patientUid = managedPatientUid.orEmpty()
        if (patientUid.isBlank()) {
            _message.value = "Brak wybranego podopiecznego"
            return
        }
        disposables.add(
            removeCareLink(patientUid)
                .andThen(clearActivePatient().onErrorComplete())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _navTarget.value = PatientSettingsNavTarget.CAREGIVER_HOME
                }, { error ->
                    _message.value = error.message ?: "Nie udalo sie usunac powiazania"
                })
        )
    }

    fun onDeletePromptHandled() {
        _deletePrompt.value = null
    }

    fun onNavigationHandled() {
        _navTarget.value = null
    }

    fun onMessageHandled() {
        _message.value = null
    }

    private fun updateOverview(transform: (SettingsOverview) -> SettingsOverview) {
        val base = currentOverview ?: SettingsOverview(
            personalData = fallbackPersonalData,
            diseases = emptyList(),
            medications = emptyList()
        )
        currentOverview = transform(base)
        val overview = currentOverview ?: return
        _uiState.value = PatientSettingsUiState.from(overview, currentCaregivers, currentDoctors)
    }

    private fun handleManagedState(state: ManagedUserUidState) {
        when (state) {
            is ManagedUserUidState.Available -> {
                managedRole = state.role
                managedPatientUid = state.uid
            }
            ManagedUserUidState.MissingActivePatient -> {
                managedRole = UserRole.CAREGIVER
                managedPatientUid = null
            }
        }
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

}

sealed class PatientSettingsDeletePrompt {
    object Self : PatientSettingsDeletePrompt()
    object Caregiver : PatientSettingsDeletePrompt()
}

enum class PatientSettingsNavTarget {
    STARTUP_GATE,
    CAREGIVER_HOME
}

package zdrowy.senior.io.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import zdrowy.senior.io.data.agent.FirestoreAccessCodeRepository
import zdrowy.senior.io.data.agent.FirestoreAgentRepository
import zdrowy.senior.io.data.alert.FirestoreAlertRepository
import zdrowy.senior.io.data.carelink.FirestoreCareLinkRepository
import zdrowy.senior.io.data.firestore.PatientUidProvider
import zdrowy.senior.io.data.measurement.FirestoreMeasurementRepository
import zdrowy.senior.io.data.notification.NoOpNotificationRepository
import zdrowy.senior.io.data.settings.FirestoreSettingsRepository
import zdrowy.senior.io.domain.agent.AccessCodeRepository
import zdrowy.senior.io.domain.agent.AddAgentUseCase
import zdrowy.senior.io.domain.agent.AddDoctorUseCase
import zdrowy.senior.io.domain.agent.AgentRepository
import zdrowy.senior.io.domain.agent.GenerateAccessCodeForAgentUseCase
import zdrowy.senior.io.domain.agent.ListAgentsUseCase
import zdrowy.senior.io.domain.agent.NotificationRepository
import zdrowy.senior.io.domain.agent.NotifyAgentsUseCase
import zdrowy.senior.io.domain.agent.ObserveAgentsUseCase
import zdrowy.senior.io.domain.agent.RemoveAgentUseCase
import zdrowy.senior.io.domain.agent.SendAccessCodeSmsUseCase
import zdrowy.senior.io.domain.agent.UpdateAgentUseCase
import zdrowy.senior.io.domain.alert.AlertRepository
import zdrowy.senior.io.domain.alert.GetAlertConfigUseCase
import zdrowy.senior.io.domain.alert.ObserveAlertConfigUseCase
import zdrowy.senior.io.domain.alert.SetAlertEnabledUseCase
import zdrowy.senior.io.domain.alert.UpdateAlertCaregiversUseCase
import zdrowy.senior.io.domain.alert.UpdateAlertChannelsUseCase
import zdrowy.senior.io.domain.alert.UpdateAlertConfigUseCase
import zdrowy.senior.io.domain.alert.UpdateCriticalThresholdsUseCase
import zdrowy.senior.io.domain.alert.UpdateSpikeRulesUseCase
import zdrowy.senior.io.domain.carelink.CareLinkRepository
import zdrowy.senior.io.domain.carelink.ConsumeCareLinkCodeUseCase
import zdrowy.senior.io.domain.carelink.GenerateCareLinkCodeUseCase
import zdrowy.senior.io.domain.carelink.ObserveCareLinksUseCase
import zdrowy.senior.io.domain.history.GetHistoryFiltersUseCase
import zdrowy.senior.io.domain.history.GetMeasurementChartDataUseCase
import zdrowy.senior.io.domain.history.GetMeasurementHistoryUseCase
import zdrowy.senior.io.domain.measurement.AddBloodPressureMeasurementUseCase
import zdrowy.senior.io.domain.measurement.AddMeasurementUseCase
import zdrowy.senior.io.domain.measurement.DeleteMeasurementUseCase
import zdrowy.senior.io.domain.measurement.GetRecentMeasurementsUseCase
import zdrowy.senior.io.domain.measurement.MeasurementRepository
import zdrowy.senior.io.domain.measurement.ObserveMeasurementChartDataUseCase
import zdrowy.senior.io.domain.measurement.ObserveMeasurementHistoryUseCase
import zdrowy.senior.io.domain.measurement.ObserveRecentMeasurementsUseCase
import zdrowy.senior.io.domain.measurement.UpdateBloodPressureMeasurementUseCase
import zdrowy.senior.io.domain.measurement.UpdateMeasurementUseCase
import zdrowy.senior.io.domain.settings.AddDiseaseUseCase
import zdrowy.senior.io.domain.settings.AddMedicationUseCase
import zdrowy.senior.io.domain.settings.GetPersonalDataUseCase
import zdrowy.senior.io.domain.settings.GetSettingsOverviewUseCase
import zdrowy.senior.io.domain.settings.ListDiseasesUseCase
import zdrowy.senior.io.domain.settings.ListMedicationsUseCase
import zdrowy.senior.io.domain.settings.ObserveDiseasesUseCase
import zdrowy.senior.io.domain.settings.ObserveMedicationsUseCase
import zdrowy.senior.io.domain.settings.ObservePersonalDataUseCase
import zdrowy.senior.io.domain.settings.RemoveDiseaseUseCase
import zdrowy.senior.io.domain.settings.RemoveMedicationUseCase
import zdrowy.senior.io.domain.settings.SettingsRepository
import zdrowy.senior.io.domain.settings.ToggleMedicationNotificationsUseCase
import zdrowy.senior.io.domain.settings.UpdateDiseaseUseCase
import zdrowy.senior.io.domain.settings.UpdateMedicationUseCase
import zdrowy.senior.io.domain.settings.UpsertPersonalDataUseCase
import zdrowy.senior.io.domain.user.EnsureUserProfileUseCase
import zdrowy.senior.io.domain.user.ActivePatientContext
import zdrowy.senior.io.domain.user.ClearActivePatientUseCase
import zdrowy.senior.io.domain.user.CurrentUserRoleContext
import zdrowy.senior.io.domain.user.GetCurrentUserRoleUseCase
import zdrowy.senior.io.domain.user.InMemoryActivePatientContext
import zdrowy.senior.io.domain.user.InMemoryCurrentUserRoleContext
import zdrowy.senior.io.domain.user.ObserveActivePatientUseCase
import zdrowy.senior.io.domain.user.ObserveCurrentUserRoleUseCase
import zdrowy.senior.io.domain.user.SetActivePatientUseCase
import zdrowy.senior.io.domain.user.SetCurrentUserRoleUseCase
import zdrowy.senior.io.domain.user.UserProfileRepository
import zdrowy.senior.io.data.user.FirestoreUserProfileRepository
import zdrowy.senior.io.ui.caregiver.CaregiverLinkViewModel
import zdrowy.senior.io.ui.patient.PatientAgentsViewModel
import zdrowy.senior.io.ui.patient.PatientAlertsViewModel
import zdrowy.senior.io.ui.patient.PatientHistoryViewModel
import zdrowy.senior.io.ui.patient.PatientHomeViewModel
import zdrowy.senior.io.ui.patient.PatientMeasurementDialogViewModel
import zdrowy.senior.io.ui.patient.PatientNotifyAgentsViewModel
import zdrowy.senior.io.ui.patient.PatientAddMedViewModel
import zdrowy.senior.io.ui.patient.PatientAddDiseaseViewModel
import zdrowy.senior.io.ui.patient.PatientEditDiseaseViewModel
import zdrowy.senior.io.ui.patient.PatientEditMedViewModel
import zdrowy.senior.io.ui.patient.PatientPersonalDataViewModel
import zdrowy.senior.io.ui.patient.PatientSettingsViewModel

val domainModule = module {
    factory { AddMeasurementUseCase(get()) }
    factory { AddBloodPressureMeasurementUseCase(get()) }
    factory { UpdateMeasurementUseCase(get()) }
    factory { UpdateBloodPressureMeasurementUseCase(get()) }
    factory { DeleteMeasurementUseCase(get()) }
    factory { GetRecentMeasurementsUseCase(get()) }
    factory { GetMeasurementHistoryUseCase(get()) }
    factory { GetMeasurementChartDataUseCase(get()) }
    factory { GetHistoryFiltersUseCase(get()) }
    factory { ObserveRecentMeasurementsUseCase(get()) }
    factory { ObserveMeasurementHistoryUseCase(get()) }
    factory { ObserveMeasurementChartDataUseCase(get()) }

    factory { AddAgentUseCase(get()) }
    factory { AddDoctorUseCase(get()) }
    factory { UpdateAgentUseCase(get()) }
    factory { RemoveAgentUseCase(get()) }
    factory { ListAgentsUseCase(get()) }
    factory { ObserveAgentsUseCase(get()) }
    factory { GenerateAccessCodeForAgentUseCase(get()) }
    factory { SendAccessCodeSmsUseCase(get()) }
    factory { NotifyAgentsUseCase(get()) }

    factory { GetAlertConfigUseCase(get()) }
    factory { ObserveAlertConfigUseCase(get()) }
    factory { UpdateAlertConfigUseCase(get()) }
    factory { SetAlertEnabledUseCase(get()) }
    factory { UpdateCriticalThresholdsUseCase(get()) }
    factory { UpdateSpikeRulesUseCase(get()) }
    factory { UpdateAlertChannelsUseCase(get()) }
    factory { UpdateAlertCaregiversUseCase(get()) }

    factory { ObserveCareLinksUseCase(get()) }
    factory { GenerateCareLinkCodeUseCase(get()) }
    factory { ConsumeCareLinkCodeUseCase(get()) }

    factory { GetPersonalDataUseCase(get()) }
    factory { GetSettingsOverviewUseCase(get()) }
    factory { UpsertPersonalDataUseCase(get()) }
    factory { ObservePersonalDataUseCase(get()) }
    factory { ObserveDiseasesUseCase(get()) }
    factory { ObserveMedicationsUseCase(get()) }
    factory { AddDiseaseUseCase(get()) }
    factory { UpdateDiseaseUseCase(get()) }
    factory { RemoveDiseaseUseCase(get()) }
    factory { ListDiseasesUseCase(get()) }
    factory { AddMedicationUseCase(get()) }
    factory { UpdateMedicationUseCase(get()) }
    factory { RemoveMedicationUseCase(get()) }
    factory { ListMedicationsUseCase(get()) }
    factory { ToggleMedicationNotificationsUseCase(get()) }

    factory { EnsureUserProfileUseCase(get()) }
    factory { SetCurrentUserRoleUseCase(get()) }
    factory { ObserveCurrentUserRoleUseCase(get()) }
    factory { GetCurrentUserRoleUseCase(get()) }
    factory { SetActivePatientUseCase(get()) }
    factory { ClearActivePatientUseCase(get()) }
    factory { ObserveActivePatientUseCase(get()) }
}

val dataModule = module {
    single<ActivePatientContext> { InMemoryActivePatientContext() }
    single<CurrentUserRoleContext> { InMemoryCurrentUserRoleContext() }
    single { PatientUidProvider(get()) }

    single<MeasurementRepository> { FirestoreMeasurementRepository(get()) }
    single<AgentRepository> { FirestoreAgentRepository(get()) }
    single<AccessCodeRepository> { FirestoreAccessCodeRepository(get()) }
    single<CareLinkRepository> { FirestoreCareLinkRepository(get()) }
    single<NotificationRepository> { NoOpNotificationRepository() }
    single<AlertRepository> { FirestoreAlertRepository(get()) }
    single<SettingsRepository> { FirestoreSettingsRepository(get()) }

    single<UserProfileRepository> { FirestoreUserProfileRepository() }
}

val uiModule = module {
    // ViewModel bindings will be added here.
}

val viewModelModule = module {
    viewModel { CaregiverLinkViewModel(get(), get(), get(), get(), get()) }
    viewModel { PatientHistoryViewModel(get(), get(), get()) }
    viewModel { PatientAgentsViewModel(get()) }
    viewModel { PatientAlertsViewModel(get()) }
    viewModel { PatientHomeViewModel(get()) }
    viewModel { PatientSettingsViewModel(get(), get(), get(), get(), get()) }
    viewModel { PatientMeasurementDialogViewModel(get(), get(), get(), get(), get()) }
    viewModel { PatientNotifyAgentsViewModel(get(), get()) }
    viewModel { PatientPersonalDataViewModel(get(), get()) }
    viewModel { PatientAddDiseaseViewModel(get()) }
    viewModel { PatientEditDiseaseViewModel(get(), get(), get()) }
    viewModel { PatientAddMedViewModel(get()) }
    viewModel { PatientEditMedViewModel(get(), get(), get()) }
}

val appModules = listOf(domainModule, dataModule, viewModelModule, uiModule)

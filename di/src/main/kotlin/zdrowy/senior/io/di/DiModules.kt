package zdrowy.senior.io.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import zdrowy.senior.io.data.agent.FirestoreAccessCodeRepository
import zdrowy.senior.io.data.agent.FirestoreAgentRepository
import zdrowy.senior.io.data.alert.FirestoreAlertRepository
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
import zdrowy.senior.io.domain.agent.RemoveAgentUseCase
import zdrowy.senior.io.domain.agent.SendAccessCodeSmsUseCase
import zdrowy.senior.io.domain.agent.UpdateAgentUseCase
import zdrowy.senior.io.domain.alert.AlertRepository
import zdrowy.senior.io.domain.alert.GetAlertConfigUseCase
import zdrowy.senior.io.domain.alert.SetAlertEnabledUseCase
import zdrowy.senior.io.domain.alert.UpdateAlertCaregiversUseCase
import zdrowy.senior.io.domain.alert.UpdateAlertChannelsUseCase
import zdrowy.senior.io.domain.alert.UpdateAlertConfigUseCase
import zdrowy.senior.io.domain.alert.UpdateCriticalThresholdsUseCase
import zdrowy.senior.io.domain.alert.UpdateSpikeRulesUseCase
import zdrowy.senior.io.domain.history.GetHistoryFiltersUseCase
import zdrowy.senior.io.domain.history.GetMeasurementChartDataUseCase
import zdrowy.senior.io.domain.history.GetMeasurementHistoryUseCase
import zdrowy.senior.io.domain.measurement.AddBloodPressureMeasurementUseCase
import zdrowy.senior.io.domain.measurement.AddMeasurementUseCase
import zdrowy.senior.io.domain.measurement.GetRecentMeasurementsUseCase
import zdrowy.senior.io.domain.measurement.MeasurementRepository
import zdrowy.senior.io.domain.settings.AddDiseaseUseCase
import zdrowy.senior.io.domain.settings.AddMedicationUseCase
import zdrowy.senior.io.domain.settings.GetPersonalDataUseCase
import zdrowy.senior.io.domain.settings.GetSettingsOverviewUseCase
import zdrowy.senior.io.domain.settings.ListDiseasesUseCase
import zdrowy.senior.io.domain.settings.ListMedicationsUseCase
import zdrowy.senior.io.domain.settings.RemoveDiseaseUseCase
import zdrowy.senior.io.domain.settings.RemoveMedicationUseCase
import zdrowy.senior.io.domain.settings.SettingsRepository
import zdrowy.senior.io.domain.settings.ToggleMedicationNotificationsUseCase
import zdrowy.senior.io.domain.settings.UpdateDiseaseUseCase
import zdrowy.senior.io.domain.settings.UpdateMedicationUseCase
import zdrowy.senior.io.domain.settings.UpsertPersonalDataUseCase
import zdrowy.senior.io.domain.user.EnsureUserProfileUseCase
import zdrowy.senior.io.domain.user.UserProfileRepository
import zdrowy.senior.io.data.user.FirestoreUserProfileRepository
import zdrowy.senior.io.ui.patient.PatientHistoryViewModel

val domainModule = module {
    factory { AddMeasurementUseCase(get()) }
    factory { AddBloodPressureMeasurementUseCase(get()) }
    factory { GetRecentMeasurementsUseCase(get()) }
    factory { GetMeasurementHistoryUseCase(get()) }
    factory { GetMeasurementChartDataUseCase(get()) }
    factory { GetHistoryFiltersUseCase(get()) }

    factory { AddAgentUseCase(get()) }
    factory { AddDoctorUseCase(get()) }
    factory { UpdateAgentUseCase(get()) }
    factory { RemoveAgentUseCase(get()) }
    factory { ListAgentsUseCase(get()) }
    factory { GenerateAccessCodeForAgentUseCase(get()) }
    factory { SendAccessCodeSmsUseCase(get()) }

    factory { GetAlertConfigUseCase(get()) }
    factory { UpdateAlertConfigUseCase(get()) }
    factory { SetAlertEnabledUseCase(get()) }
    factory { UpdateCriticalThresholdsUseCase(get()) }
    factory { UpdateSpikeRulesUseCase(get()) }
    factory { UpdateAlertChannelsUseCase(get()) }
    factory { UpdateAlertCaregiversUseCase(get()) }

    factory { GetPersonalDataUseCase(get()) }
    factory { GetSettingsOverviewUseCase(get()) }
    factory { UpsertPersonalDataUseCase(get()) }
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
}

val dataModule = module {
    single<MeasurementRepository> { FirestoreMeasurementRepository() }
    single<AgentRepository> { FirestoreAgentRepository() }
    single<AccessCodeRepository> { FirestoreAccessCodeRepository() }
    single<NotificationRepository> { NoOpNotificationRepository() }
    single<AlertRepository> { FirestoreAlertRepository() }
    single<SettingsRepository> { FirestoreSettingsRepository() }

    single<UserProfileRepository> { FirestoreUserProfileRepository() }
}

val uiModule = module {
    // ViewModel bindings will be added here.
}

val viewModelModule = module {
    viewModel { PatientHistoryViewModel(get(), get(), get()) }
}

val appModules = listOf(domainModule, dataModule, viewModelModule, uiModule)

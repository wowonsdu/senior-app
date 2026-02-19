package zdrowy.senior.io.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import zdrowy.senior.io.data.auth.FirebaseAuthRepository
import zdrowy.senior.io.data.alert.InMemoryAlertSettingsRepository
import zdrowy.senior.io.data.invite.FirebaseInviteRepository
import zdrowy.senior.io.data.contact.InMemoryContactRepository
import zdrowy.senior.io.data.measurement.InMemoryMeasurementRepository
import zdrowy.senior.io.data.session.SharedPrefsSessionRepository
import zdrowy.senior.io.data.settings.InMemorySettingsRepository
import zdrowy.senior.io.data.visit.InMemoryVisitRepository
import zdrowy.senior.io.domain.alert.AlertSettingsRepository
import zdrowy.senior.io.domain.alert.ObserveAlertSettingsUseCase
import zdrowy.senior.io.domain.alert.UpdateAlertSettingsUseCase
import zdrowy.senior.io.domain.auth.AuthRepository
import zdrowy.senior.io.domain.auth.GetCurrentSessionUseCase
import zdrowy.senior.io.domain.auth.SendOtpUseCase
import zdrowy.senior.io.domain.auth.SignOutUseCase
import zdrowy.senior.io.domain.auth.VerifyOtpUseCase
import zdrowy.senior.io.domain.contact.AddContactUseCase
import zdrowy.senior.io.domain.contact.ContactRepository
import zdrowy.senior.io.domain.contact.DeleteContactUseCase
import zdrowy.senior.io.domain.contact.ObserveContactsUseCase
import zdrowy.senior.io.domain.invite.ClaimInviteUseCase
import zdrowy.senior.io.domain.invite.InviteRepository
import zdrowy.senior.io.domain.invite.ResolveInviteUseCase
import zdrowy.senior.io.domain.measurement.AddMeasurementUseCase
import zdrowy.senior.io.domain.measurement.MeasurementRepository
import zdrowy.senior.io.domain.measurement.ObserveMeasurementsUseCase
import zdrowy.senior.io.domain.session.ClearSessionUseCase
import zdrowy.senior.io.domain.session.GetActivePatientUseCase
import zdrowy.senior.io.domain.session.GetSessionUseCase
import zdrowy.senior.io.domain.session.SessionRepository
import zdrowy.senior.io.domain.session.SetActivePatientUseCase
import zdrowy.senior.io.domain.session.SetSessionUseCase
import zdrowy.senior.io.domain.settings.AddDiseaseUseCase
import zdrowy.senior.io.domain.settings.AddMedicineUseCase
import zdrowy.senior.io.domain.settings.GetPersonalInfoUseCase
import zdrowy.senior.io.domain.settings.ObserveDiseasesUseCase
import zdrowy.senior.io.domain.settings.ObserveMedicinesUseCase
import zdrowy.senior.io.domain.settings.SettingsRepository
import zdrowy.senior.io.domain.settings.UpdatePersonalInfoUseCase
import zdrowy.senior.io.domain.visit.AddVisitUseCase
import zdrowy.senior.io.domain.visit.ObserveVisitsUseCase
import zdrowy.senior.io.domain.visit.VisitRepository
import zdrowy.senior.io.ui.caregiver.InMemoryPatientStore
import zdrowy.senior.io.ui.caregiver.PatientStore

val appModule = module {
    single { androidContext().getSharedPreferences("session_store", 0) }
    single { FirebaseAuth.getInstance() }
    single { FirebaseFunctions.getInstance() }
    single<AuthRepository> { FirebaseAuthRepository(get()) }
    single<InviteRepository> { FirebaseInviteRepository(get()) }
    single<SessionRepository> { SharedPrefsSessionRepository(get()) }
    single<MeasurementRepository> { InMemoryMeasurementRepository() }
    single<ContactRepository> { InMemoryContactRepository() }
    single<VisitRepository> { InMemoryVisitRepository() }
    single<SettingsRepository> { InMemorySettingsRepository() }
    single<AlertSettingsRepository> { InMemoryAlertSettingsRepository() }

    factory { SendOtpUseCase(get()) }
    factory { VerifyOtpUseCase(get()) }
    factory { GetCurrentSessionUseCase(get()) }
    factory { SignOutUseCase(get()) }
    factory { ResolveInviteUseCase(get()) }
    factory { ClaimInviteUseCase(get()) }
    factory { GetSessionUseCase(get()) }
    factory { SetSessionUseCase(get()) }
    factory { ClearSessionUseCase(get()) }
    factory { GetActivePatientUseCase(get()) }
    factory { SetActivePatientUseCase(get()) }
    factory { AddMeasurementUseCase(get()) }
    factory { ObserveMeasurementsUseCase(get()) }
    factory { AddContactUseCase(get()) }
    factory { ObserveContactsUseCase(get()) }
    factory { DeleteContactUseCase(get()) }
    factory { AddVisitUseCase(get()) }
    factory { ObserveVisitsUseCase(get()) }
    factory { GetPersonalInfoUseCase(get()) }
    factory { UpdatePersonalInfoUseCase(get()) }
    factory { AddMedicineUseCase(get()) }
    factory { ObserveMedicinesUseCase(get()) }
    factory { AddDiseaseUseCase(get()) }
    factory { ObserveDiseasesUseCase(get()) }
    factory { ObserveAlertSettingsUseCase(get()) }
    factory { UpdateAlertSettingsUseCase(get()) }

    single<PatientStore> { InMemoryPatientStore() }
}

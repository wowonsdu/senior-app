# Etap 4.2 — Use case’y + dummy repo (Pacjent)

## Summary
Implementujemy wszystkie use case’y potrzebne do ekranow pacjenta, z dummy/in-memory repo. Praca podzielona tematycznie. Po zakonczonych pracach sprawdzamy spojnosc (interfejsy, DI, kompilacja).

## Lista wymaganych use case’ow

### Pomiary (Pacjent)
- UC-MEAS-01 AddMeasurement
- UC-MEAS-02 AddBloodPressureMeasurement
- UC-MEAS-03 GetRecentMeasurements

### Historia pomiarow
- UC-HIST-01 GetMeasurementHistory
- UC-HIST-02 GetMeasurementChartData
- UC-HIST-03 GetHistoryFilters

### Agenci i lekarze
- UC-AGENT-01 AddAgent
- UC-AGENT-02 AddDoctor
- UC-AGENT-03 UpdateAgent
- UC-AGENT-04 RemoveAgent
- UC-AGENT-05 ListAgents
- UC-AGENT-06 GenerateAccessCodeForAgent
- UC-AGENT-07 SendAccessCodeSms

### Alerty i powiadomienia (konfiguracja)
- UC-ALERT-01 GetAlertConfig
- UC-ALERT-02 UpdateAlertConfig
- UC-ALERT-03 SetAlertEnabled
- UC-ALERT-04 UpdateCriticalThresholds
- UC-ALERT-05 UpdateSpikeRules
- UC-ALERT-06 UpdateAlertChannels
- UC-ALERT-07 UpdateAlertCaregivers

### Ustawienia pacjenta
- UC-SET-01 GetPersonalData
- UC-SET-02 UpsertPersonalData
- UC-SET-03 AddDisease
- UC-SET-04 UpdateDisease
- UC-SET-05 RemoveDisease
- UC-SET-06 ListDiseases
- UC-SET-07 AddMedication
- UC-SET-08 UpdateMedication
- UC-SET-09 RemoveMedication
- UC-SET-10 ListMedications
- UC-SET-11 ToggleMedicationNotifications

## Repozytoria wymagane
- MeasurementRepository
- AgentRepository
- AccessCodeRepository
- NotificationRepository (mock)
- AlertRepository
- SettingsRepository

## Podzial prac (tematyczny)

### Pomiary + Historia
- UC-MEAS-01..03, UC-HIST-01..03
- Dummy MeasurementRepository
- Modele: Measurement, MeasurementType, ChartSeries, HistoryFilterState

### Agenci i lekarze
- UC-AGENT-01..07
- Dummy AgentRepository, AccessCodeRepository, NotificationRepository
- Modele: Agent, AgentDraft, AgentUpdate, DoctorDraft, AccessCode

### Alerty
- UC-ALERT-01..07
- Dummy AlertRepository
- Modele: AlertConfig, progi, kanaly

### Ustawienia pacjenta
- UC-SET-01..11
- Dummy SettingsRepository
- Modele: PersonalData, Disease, Medication

## Integracja koncowa
1) Sprawdzenie spojnosc kontraktow i nazw
2) DI (Koin): repo + use case’y
3) Kompilacja / sanity check
4) Weryfikacja danych dummy

## Assumptions
- Bez Auth i sesji w Etapie 4.2
- Dummy repo wystarczaja do obslugi UI bez Firebase

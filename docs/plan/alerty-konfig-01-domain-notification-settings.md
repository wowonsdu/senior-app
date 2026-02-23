# Etap 1 — Domain API (NotificationSettings)

## Scope
- Domena: nowy model ustawień `NotificationSettings` dla konfiguracji alertów użytkownika.
- Repo + use case’y do pobrania/obserwacji i aktualizacji ustawień alertów.
- Przepięcie `TriggerBloodPressureAlertEventUseCase` na nowe źródło konfiguracji.

## Modules And Layers
- `domain`

## Deliverables
- `NotificationSettings` (settings usera) w domain.
- `NotificationSettingsRepository` jako interfejs repo.
- Use case’y: get/observe + update per typ (`enabled`, progi, spike, kanały, opiekunowie).
- `TriggerBloodPressureAlertEventUseCase` czyta config z `NotificationSettings`.

## Implementation Checklist
- [ ] Dodać `NotificationSettings` (lista `AlertSetting` per `MeasurementType`).
- [ ] Dodać `NotificationSettingsRepository` (API analogiczne do obecnego configu alertów).
- [ ] Dodać nowe use case’y z `operator fun invoke(...)`.
- [ ] Przepiąć `TriggerBloodPressureAlertEventUseCase` na `NotificationSettings`.

## Validation
- [ ] Kompilacja modułu `domain`.


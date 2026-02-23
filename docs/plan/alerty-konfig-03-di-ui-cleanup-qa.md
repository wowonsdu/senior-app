# Etap 3 — DI + UI switch + cleanup + QA

## Scope
- DI/UI: przepięcie `PatientAlertsViewModel` na nowe use case’y.
- Cleanup: wycofanie starego repo/configu alertów.
- QA: smoke test ustawień alertów + generowanie `alertEvents` (ciśnienie).

## Modules And Layers
- `di`, `ui`, `domain`, `data`

## Implementation Checklist
- [ ] Koin: bindowanie `NotificationSettingsRepository` + use case’y + VM.
- [ ] UI: `PatientAlertsViewModel` mapuje `NotificationSettings` na UI.
- [ ] Cleanup: usunąć stary `AlertRepository` (config) + `FirestoreAlertRepository` (config) + use case’y.
- [ ] Docs: zaktualizować `docs/PLAN.md` (checklist) i ewentualnie `docs/USE_CASES.md`.
- [ ] Smoke: ustawienia alertów (toggle kanałów/opiekunów + zapis progów ciśnienia).

## Validation
- [ ] `assembleDebug` / testy.


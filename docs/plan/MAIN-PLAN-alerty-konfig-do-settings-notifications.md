# Plan — Alerty: config -> `users/{uid}/settings/notifications`

Cel: trzymać konfigurację alertów (progi/spike/kanały/opiekunowie) w jednym dokumencie `users/{uid}/settings/notifications` oraz wprowadzić domenowy model `NotificationSettings` w ustawieniach użytkownika.

## Etap 1 — Domain API (NotificationSettings)
- [ ] `NotificationSettings` + repo + use case’y w domain
- [ ] Przepięcie `TriggerBloodPressureAlertEventUseCase` na nowe źródło konfiguracji
- [ ] Subplan: docs/plan/alerty-konfig-01-domain-notification-settings.md

## Etap 2 — Data + Firestore settings/notifications
- [ ] Repo Firestore czyta/zapisuje `users/{uid}/settings/notifications`
- [ ] Brak migracji — w bazie istnieje tylko nowa struktura
- [ ] Subplan: docs/plan/alerty-konfig-02-data-firestore-settings-notifications.md

## Etap 3 — DI + UI switch + cleanup + QA
- [ ] Koin: nowe bindingi repo/use case’ów
- [ ] UI: `PatientAlertsViewModel` korzysta z nowych use case’ów
- [ ] Cleanup: usunięcie starego `AlertRepository` (config) i `FirestoreAlertRepository` (config)
- [ ] QA: smoke test ustawień alertów + generowanie `alertEvents` (ciśnienie)
- [ ] Subplan: docs/plan/alerty-konfig-03-di-ui-cleanup-qa.md

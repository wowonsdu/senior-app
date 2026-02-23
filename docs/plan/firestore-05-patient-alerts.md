# Firestore — alerty (config w settings/notifications)

## Scope
- Konfiguracja alertów jako ustawienia użytkownika: `users/{uid}/settings/notifications`.
- Jeden dokument z mapą `alerts.{measurementType}.*` zamiast subkolekcji per-typ.
- Migracja/seed ze starej ścieżki `users/{uid}/alerts/{measurementType}`.

## Modules And Layers
- domain: `NotificationSettingsRepository`, `NotificationSettings`, `AlertSetting`, `AlertChannel`
- data: `FirestoreNotificationSettingsRepository`

## Deliverables
- `users/{uid}/settings/notifications`:
  - `alerts.{measurementType}.enabled/min/max/spikePercent/windowCount/channels/caregiverUids`
  - `alerts.PRESSURE.systolicMin/systolicMax/diastolicMin/diastolicMax` (+ kompatybilne `min/max` jako SYS)

## Implementation Checklist
- [ ] Odczyt nowego doc + fallback do `/alerts/*` + seed (migracja) do `settings/notifications`.
- [ ] Update per-typ (merge) bez nadpisywania innych typów.
- [ ] Obserwacja snapshotów nowego dokumentu.

## Validation
- [ ] Emulator: po wejściu w ekran konfiguracji doc `settings/notifications` istnieje i aktualizuje się po zmianach.


# Etap 2 — Data + Firestore settings/notifications

## Scope
- Data: ścieżka dla konfiguracji alertów: `users/{uid}/settings/notifications`.

## Modules And Layers
- `data`

## Deliverables
- `FirestorePaths.NOTIFICATIONS = "notifications"`.
- `FirestoreNotificationSettingsRepository`:
  - read: nowy doc
  - write: zawsze nowy doc (merge, bez nadpisywania innych typów)

## Implementation Checklist
- [ ] Dodać path: `users/{uid}/settings/notifications`.
- [ ] Implementacja `getNotificationSettings()` (fallback do defaultów, bez migracji).
- [ ] Implementacja `observeNotificationSettings()` oparta o snapshot doc.
- [ ] Implementacja update per typ: zapis pod `alerts[type]` (merge).

## Validation
- [ ] Emulator: UI odczytuje dane z `settings/notifications`.

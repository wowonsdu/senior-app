# Etap 2 — Data + Firestore migracja

## Scope
- Data: nowa ścieżka dla konfiguracji alertów: `users/{uid}/settings/notifications`.
- Migracja: fallback odczytu z `users/{uid}/alerts/{measurementType}` + seed do nowego dokumentu.

## Modules And Layers
- `data`

## Deliverables
- `FirestorePaths.NOTIFICATIONS = "notifications"`.
- `FirestoreNotificationSettingsRepository`:
  - read: nowy doc
  - fallback: stara kolekcja `/alerts`
  - write: zawsze nowy doc (merge, bez nadpisywania innych typów)

## Implementation Checklist
- [ ] Dodać path: `users/{uid}/settings/notifications`.
- [ ] Implementacja `getNotificationSettings()` z migracją/seedem.
- [ ] Implementacja `observeNotificationSettings()` oparta o snapshot doc.
- [ ] Implementacja update per typ: zapis pod `alerts[type]` (merge).

## Validation
- [ ] Emulator: seed do nowej ścieżki działa, UI odczytuje dane z `settings/notifications`.


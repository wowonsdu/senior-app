# Firestore — patient settings plan (personalData/diseases/medications)

## Scope
- Mapowanie `SettingsRepository` na Firestore dla pacjenta.

## Modules And Layers
- domain: `SettingsRepository`, modele settings
- data: `FirestoreSettingsRepository`

## Deliverables
- `users/{patientUid}/settings/personalData` (1 doc).
- `users/{patientUid}/diseases/*` i `users/{patientUid}/medications/*`.

## Implementation Checklist
- [ ] get + upsert personalData (merge) + `updatedAt`.
- [ ] CRUD dla diseases.
- [ ] CRUD dla medications + toggle notifications.

## Validation
- [ ] Emulator: zapis personalData i odczyt po restarcie app.
- [ ] Emulator: dodanie/edycja/usunięcie choroby i leku.


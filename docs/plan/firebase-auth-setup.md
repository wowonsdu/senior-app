# Firebase Auth — Setup Plan

## Scope
- Tylko Firebase Auth (phone/SMS).
- DEBUG domyslnie uzywa Auth Emulator Suite.
- Bez Firestore i bez produkcyjnej konfiguracji (ta moze wejsc pozniej).

## Modules And Layers
- `app` (init Firebase, emulator)
- `ui` (ekrany logowania i weryfikacji)
- `gradle` (version catalog, pluginy, deps)

## Deliverables
- Firebase deps w modulach, ktore realnie uzywaja SDK.
- `SeniorApp` inicjalizuje Firebase oraz podpina emulator w DEBUG.

## Implementation Checklist
- [ ] Dodaj Firebase Auth deps do `app` i `ui` (BOM + auth)
- [ ] DEBUG flaga: `USE_FIREBASE_AUTH_EMULATOR` + host/port
- [ ] `SeniorApp`: init Firebase (fallback w DEBUG, jesli brak `google-services.json`)
- [ ] `SeniorApp`: `FirebaseAuth.useEmulator(...)` + `setAppVerificationDisabledForTesting(true)` w DEBUG

## Validation
- [ ] `:app:assembleDebug` przechodzi
- [ ] Logcat: brak crasha z "Default FirebaseApp is not initialized"


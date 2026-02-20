# Firebase Auth — Validation Plan (emulator-5554)

## Scope
- Smoke test na emulatorze Android: kliki + inputy + przejscia przez SMS verify.
- Instrukcja uruchomienia Auth Emulator Suite.

## Deliverables
- Skrypt PowerShell do odpalenia scenariusza (Pacjent + Opiekun).
- Minimalna dokumentacja deweloperska.

## Implementation Checklist
- [ ] Dodaj `scripts/smoke_auth_phone.ps1` (uiautomator/ADB)
- [ ] Dokumentacja: `docs/FIREBASE_AUTH_EMULATOR.md`
- [ ] Uruchom smoke na `emulator-5554`

## Validation
- [ ] Pacjent flow przechodzi do `patientHomeFragment`
- [ ] Opiekun flow przechodzi do placeholdera


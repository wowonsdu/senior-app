# Firebase Auth — Opiekun (Phone/SMS) Plan

## Scope
- Ekran `CaregiverLoginFragment`: wyslanie SMS dla numeru tel.
- Ekran `CaregiverSmsVerifyFragment`: weryfikacja kodu + zalogowanie.
- Po sukcesie: przejscie do placeholder ekranu opiekuna (tymczasowo).

## Modules And Layers
- `ui` (fragmenty + nawigacja + placeholder)

## Deliverables
- Identyczne zachowanie jak Pacjent (bez duplikacji bugow).
- Placeholder ekran po zalogowaniu do czasu Etapu 5.

## Implementation Checklist
- [ ] CaregiverLogin: walidacja numeru tel + start `verifyPhoneNumber`
- [ ] CaregiverLogin: onCodeSent -> navigate do caregiver SMS verify z args
- [ ] CaregiverSmsVerify: `signInWithCredential` + obsluga bledow
- [ ] Dodaj placeholder `CaregiverHomeFragment` + layout
- [ ] NavGraph: akcje do placeholdera po sukcesie

## Validation
- [ ] Emulator auth: test numer, wpisz kod `123456`, wejdz do placeholdera opiekuna


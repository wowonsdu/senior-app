# Plan — Firebase Auth (Phone / SMS)

Cel: wdrozyc tylko i wylacznie Firebase Auth poprzez numer telefonu (SMS). Firestore/DB pozniej.

## Etap 1 — Setup Firebase + emulator
- [ ] Konfiguracja zaleznosci (Gradle) pod Firebase Auth
- [ ] Inicjalizacja Firebase w `SeniorApp` (fallback dla debug, jesli brak `google-services.json`)
- [ ] DEBUG: polaczenie z Auth Emulator Suite + `setAppVerificationDisabledForTesting(true)`
- [ ] Subplan: docs/plan/firebase-auth-setup.md

## Etap 2 — Pacjent: logowanie SMS
- [ ] Wyslanie kodu SMS na ekranie logowania
- [ ] Weryfikacja kodu + `signInWithCredential` na ekranie SMS
- [ ] Nawigacja do `patientHomeFragment` po sukcesie
- [ ] Subplan: docs/plan/firebase-auth-patient.md

## Etap 3 — Opiekun: logowanie SMS + placeholder po zalogowaniu
- [ ] Wyslanie kodu SMS na ekranie logowania
- [ ] Weryfikacja kodu + `signInWithCredential` na ekranie SMS
- [ ] Placeholder ekran po zalogowaniu (tymczasowo, do Etapu 5)
- [ ] Subplan: docs/plan/firebase-auth-caregiver.md

## Etap 4 — Sesja, routing, logout
- [ ] RoleSelect: jesli zalogowany, klik roli omija logowanie i przechodzi do flow roli
- [ ] Logout: `FirebaseAuth.signOut()` + powrot do RoleSelect
- [ ] Subplan: docs/plan/firebase-auth-session-routing.md

## Etap 5 — Walidacja (emulator-5554)
- [ ] Skrypt smoke (ADB/MCP): Pacjent auth (wyslij kod + wpisz `123456`)
- [ ] Skrypt smoke (ADB/MCP): Opiekun auth (wyslij kod + wpisz `123456`)
- [ ] Krótka instrukcja: uruchomienie Auth Emulator Suite
- [ ] Subplan: docs/plan/firebase-auth-validation.md


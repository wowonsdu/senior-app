# Firestore — users/{uid} + bootstrap roli plan

## Scope
- Utworzenie/utrzymanie dokumentu `users/{uid}` po zalogowaniu.
- Rola `PATIENT`/`CAREGIVER` ustawiana raz i weryfikowana przy kolejnych logowaniach.

## Modules And Layers
- UI auth flow (SMS verify)
- domain: use case + repo interface
- data: Firestore implementacja

## Deliverables
- Idempotentne `ensureCurrentUserProfile(role)`.
- Błąd przy próbie użycia innej roli niż zapisana w Firestore.

## Implementation Checklist
- [ ] Zapisać minimalne pola do `users/{uid}` przy pierwszym logowaniu.
- [ ] Przy istniejącym dokumencie: sprawdzić `role`, ewentualnie zaktualizować `lastLoginAt`.
- [ ] UI: po `signInWithCredential` wykonać `ensure...` przed nawigacją.

## Validation
- [ ] Nowe konto pacjenta/opiekuna tworzy `users/{uid}`.
- [ ] Próba wejścia w inną rolę zwraca błąd i blokuje nawigację.


# Firebase Auth — Sesja / Routing Plan

## Scope
- Start aplikacji zostaje na `RoleSelectFragment`.
- Klik roli:
  - jesli zalogowany -> omijamy login i wchodzimy do flow roli
  - jesli niezalogowany -> przechodzimy do login
- Logout: `FirebaseAuth.signOut()` oraz nawigacja do `RoleSelectFragment`.

## Modules And Layers
- `ui` (RoleSelect + logout)

## Deliverables
- Spójne zachowanie dla Pacjent i Opiekun.

## Implementation Checklist
- [ ] RoleSelect: sprawdz `FirebaseAuth.currentUser`
- [ ] RoleSelect: patient click -> `patientHomeFragment` lub `patientLoginFragment`
- [ ] RoleSelect: caregiver click -> `caregiverHomeFragment` lub `caregiverLoginFragment`
- [ ] Logout: w menu pacjenta (i placeholderze opiekuna) dodaj `FirebaseAuth.signOut()`

## Validation
- [ ] Po logout: role select, a kolejne wejscie w role idzie przez login


# Firebase Auth — Pacjent (Phone/SMS) Plan

## Scope
- Ekran `PatientLoginFragment`: wyslanie SMS dla numeru tel.
- Ekran `PatientSmsVerifyFragment`: weryfikacja kodu + zalogowanie.
- Po sukcesie: nawigacja do `patientHomeFragment`.

## Modules And Layers
- `ui` (fragmenty + nawigacja)

## Deliverables
- Walidacja numeru telefonu (minimum) + obsluga bledow.
- Przekazywanie `verificationId` + numeru tel do ekranu weryfikacji.

## Implementation Checklist
- [ ] PatientLogin: pobierz numer telefonu z `TextInputLayout.editText`
- [ ] PatientLogin: walidacja + format E.164 (tolerancja dla 9 cyfr -> `+48`)
- [ ] PatientLogin: `PhoneAuthProvider.verifyPhoneNumber(...)` + callbacks
- [ ] PatientLogin: onCodeSent -> navigate do SMS verify z args
- [ ] PatientSmsVerify: pokaz numer tel, pobierz `verificationId`
- [ ] PatientSmsVerify: `signInWithCredential` (verificationId + code)
- [ ] Auto-verify: onVerificationCompleted -> signIn + navigate

## Validation
- [ ] Emulator auth: test numer, wpisz kod `123456`, wejdz do `patientHomeFragment`


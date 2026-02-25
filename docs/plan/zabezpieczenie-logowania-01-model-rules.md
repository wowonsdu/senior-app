# Zabezpieczenie logowania — 01 — Model danych + Firestore rules

Cel:
Zablokowac globalne "get accessCodes/{code}" i ograniczyc dostep po zweryfikowanym numerze telefonu.

## Zmiany w modelu danych (accessCodes/{code})
Wymagane pola dla kodow do wpisania po SMS:
- phoneNumberE164: string (docelowy numer telefonu, ktory musi sie zgadzac z request.auth.token.phone_number)
- type: "CAREGIVER_TO_PATIENT" lub "PATIENT_TO_CAREGIVER"
- ownerUid: uid generujacego kod
- expiresAtMs: number
- createdAt: timestamp

Dodatkowe (opcjonalne, jak juz jest):
- patientUid / caregiverUid: zalezne od typu
- draftPhoneNumber: (legacy / zgodnosc) -> decyzja czy zostawiamy, czy unifikujemy na phoneNumberE164

## Firestore rules — wymagania
- accessCodes/{code}:
  - allow get: tylko gdy request.auth.token.phone_number == resource.data.phoneNumberE164
    (opcjonalnie OR ownerUid == request.auth.uid)
  - allow delete: jw.
  - allow create: jak dotychczas (zaleznie od roli), ale musi wymagac ustawienia phoneNumberE164 dla kodow "do wpisania"
  - allow list/update: false

- accessCodeDrafts/{code}:
  - utrzymac obecne zasady: get/delete tylko dla dopasowanego phone_number

## Checklist
- [ ] Spisac finalny kontrakt pol (jedna nazwa pola dla telefonu: phoneNumberE164)
- [ ] Zmienic firestore.rules zgodnie z wymaganiami
- [ ] (Jesli trzeba) firestore.indexes.json bez zmian
- [ ] Walidacja: proba odczytu kodu z innego konta/telefonu musi byc PERMISSION_DENIED


# Zabezpieczenie logowania — 02 — UI/UX flow (SMS -> kod)

Cel:
Uzytkownik nie ma dostepu do ekranu wpisania kodu przed weryfikacja SMS.

## Pacjent
Docelowy flow:
1) Wpisz numer telefonu
2) SMS verify (Firebase Phone Auth)
3) Ekran: "Podaj kod, ktory otrzymales od opiekuna"
4) Zuzyj kod -> link -> wejscie do aplikacji pacjenta

Checklist:
- [x] Zablokowac wszystkie wejscia do kodu przed SMS verify
- [x] UI bledu: bledny kod / wygasl / kod dla innego telefonu

## Opiekun
Docelowy flow generowania:
- Opiekun generuje kod dla pacjenta, ale musi podac numer telefonu pacjenta
- Kod zapisuje phoneNumberE164, zeby pacjent po SMS mogl go zuzyc

Checklist:
- [x] Formularz generowania kodu: telefon pacjenta wymagany
- [x] Normalizacja telefonu do E164 (albo jasno wymagamy E164 w UI)
- [x] UX: informacja "Pacjent musi potwierdzic ten numer SMS, potem wpisac kod"
- [x] Dodaj podopiecznego: obserwuj careLinks i zamknij ekran po polaczeniu

## PATIENT->CAREGIVER (jesli nie wylaczamy)
- [x] Wymagac telefonu opiekuna i generowac kod z phoneNumberE164 opiekuna (nie dotyczy — flow wylaczony)
- [x] Flow opiekuna: SMS -> wpisz kod od pacjenta (nie dotyczy — flow wylaczony)

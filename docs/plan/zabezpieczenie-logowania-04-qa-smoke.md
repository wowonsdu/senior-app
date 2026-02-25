# Zabezpieczenie logowania — 04 — QA + smoke

Cel:
Potwierdzic, ze nie da sie odczytac kodu bez dopasowanego numeru telefonu i ze flow dziala end-to-end.

## Scenariusze
- [ ] Pacjent: poprawny telefon -> SMS -> poprawny kod -> link ok
- [ ] Pacjent: poprawny telefon -> SMS -> bledny kod -> blad
- [ ] Pacjent: telefon A -> SMS -> kod wystawiony na telefon B -> PERMISSION_DENIED / blad "kod dla innego telefonu"
- [ ] Pacjent: kod wygasl -> blad
- [ ] Opiekun: generuje kod z telefonem pacjenta -> pacjent moze go zuzyc tylko po SMS na ten telefon

## Security sanity
- [ ] Zalogowany user X probuje get accessCodes/{random} -> zawsze PERMISSION_DENIED, chyba ze phoneNumberE164 pasuje
- [ ] Brak sciezki UI do wpisania kodu przed SMS verify

## Release smoke
- [ ] assembleRelease + test na prawdziwym projekcie Firebase (bez emulatorow)


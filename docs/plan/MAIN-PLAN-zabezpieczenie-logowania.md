# MAIN PLAN — Zabezpieczenie logowania

Cel:
Zamknac wektor brute-force na 6-cyfrowych kodach (accessCodes) bez "dziwnych" Firestore rules.
Zmieniamy flow tak, aby uzytkownik najpierw zweryfikowal numer telefonu przez SMS (Firebase Phone Auth),
a dopiero potem mogl podac kod powiazania (od opiekuna). Dostep do kodu w Firestore ograniczamy
do zweryfikowanego numeru telefonu (request.auth.token.phone_number).

## Analiza (dlaczego to dziala)
Problem dzis:
- accessCodes/{code} jest czytelne przez "get" dla kazdego zalogowanego usera (brak rate limit w rules).
- 6 cyfr = 1 000 000 kombinacji -> mozliwe zgadywanie.

Rozwiazanie:
- "Enter code" dopiero po Phone Auth (SMS).
- Kazdy kod, ktory user ma wpisac, ma przypisany docelowy numer telefonu (phoneNumberE164).
- Firestore rules pozwalaja na odczyt/zuzycie kodu tylko gdy:
  request.auth.token.phone_number == resource.data.phoneNumberE164
  (czyli tylko wlasciciel zweryfikowanego numeru).

Efekt:
- Nie da sie masowo zgadywac kodow "globalnie", bo nawet trafienie poprawnego kodu nie da odczytu bez
  kontroli nad konkretnym numerem telefonu (SMS weryfikacja).
- To nie wymaga rate limiting w rules (ktorego i tak nie ma), ani Cloud Functions.

## Decyzje (zamrozone)
- Zostawiamy 6-cyfrowe kody, ale zabezpieczamy dostepem po zweryfikowanym numerze telefonu.
- W produkcji priorytet: flow CAREGIVER -> PATIENT (opiekun generuje kod dla pacjenta z podanym telefonem).
- Flow PATIENT -> CAREGIVER:
  - MVP: wylaczone albo wymagajace podania telefonu opiekuna (do decyzji w Etapie 0).
  - Bez tego flow nadal da sie zabezpieczyc tylko dlugim tokenem/QR albo backendem.
Wybor: opcja A (PATIENT->CAREGIVER wylaczony w produkcji).

## Etap 0 — Scope i decyzja dla PATIENT->CAREGIVER
- [x] Ustalic jednoznacznie:
  - opcja A (recommended): wylacz PATIENT->CAREGIVER w produkcji (UI i UC), zostaje tylko CAREGIVER->PATIENT
  - opcja B: PATIENT->CAREGIVER wymaga telefonu opiekuna i tez zapisuje phoneNumberE164 w accessCodes
- [x] Zapisac decyzje w tym pliku (sekcja "Decyzje")

## Etap 1 — Model danych + Firestore rules (bezpieczne accessCodes)
- [x] Zdefiniowac wymagane pola w accessCodes/{code} dla kodow "do wpisania po SMS"
- [x] Zmienic rules dla accessCodes: get/delete tylko dla dopasowanego phone_number (i ewentualnie owner)
- [x] Upewnic sie, ze accessCodeDrafts pozostaje zgodne (tam juz jest powiazanie po phone)
- [x] Subplan: docs/plan/zabezpieczenie-logowania-01-model-rules.md
- [x] DocId = phoneE164 (get po numerze, jeden aktywny kod na numer)

## Etap 2 — UI/UX: najpierw SMS, potem kod
- [x] Przestawic flow tak, aby ekran wpisania kodu pojawial sie dopiero po sukcesie SMS verify
- [x] Dla pacjenta: Phone Auth -> ekran "Podaj kod od opiekuna"
- [x] Dla opiekuna: generowanie kodu wymaga telefonu pacjenta (E164 lub normalizacja)
- [x] Subplan: docs/plan/zabezpieczenie-logowania-02-ui-flow.md

## Etap 3 — Domain/Data: logika generowania i zuzywania kodu
- [x] Domain: doprecyzowac kontrakty UseCase/Repo pod nowe wymagania (phoneNumberE164)
- [x] Data: FirestoreCareLinkRepository / FirestoreAccessCodeRepository dostosowane do nowych pol
- [x] Usunac lub ukryc stare sciezki, ktore czytaja kod bez phone gate
- [x] Subplan: docs/plan/zabezpieczenie-logowania-03-domain-data.md

## Etap 4 — QA + smoke
- [ ] Testy scenariuszy (pacjent i opiekun), w tym bledne telefony, bledne kody, wygasniecie TTL
- [ ] Smoke na debug i release (z prawdziwym Firebase, bez emulatorow)
- [ ] Subplan: docs/plan/zabezpieczenie-logowania-04-qa-smoke.md

## Definicja gotowosci (DoD)
- Kod accessCodes jest nieczytelny dla userow bez dopasowanego phone_number.
- W UI nie da sie dojsc do "wpisz kod" bez przejscia SMS verify.
- Smoke pacjent/opiekun przechodzi end-to-end.

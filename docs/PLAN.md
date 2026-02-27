# Plan wykonania (checklista)## Etap ZL — Zabezpieczenie logowania (SMS -> kod)
- [x] 0. Decyzja: PATIENT->CAREGIVER wylaczony w prod (tylko CAREGIVER->PATIENT)
- [x] 1. Model + rules: accessCodes z phoneNumberE164 + ograniczenia dostepu
- [x] 1.1 accessCodes docId = phoneE164 + tylko inny numer + jeden kod aktywny
- [x] 2. UI/UX: SMS verify -> ekran kodu (bez kodu przed SMS)
- [x] 2.1 UI/UX: Dodaj podopiecznego auto-zamyka po konsumcji kodu
- [x] 3. Domain/Data: dopasowanie repo/UC pod phone gate
- [ ] 4. QA: smoke debug + release (bez emulatorow)

## Etap PR — Przygotowanie do Play Production Release (Google Play)

# MVP “Production Release” (Google Play) — Senior App

## Podsumowanie
W obecnym stanie aplikacja ma już sporo funkcji MVP (pacjent + opiekun, realtime na Firestore, STT), ale do wypuszczenia na **Play Production** brakuje kilku rzeczy, które są realnymi blockerami: **konfiguracja produkcyjnego Firebase w release**, **usunięcie debugowych wejść (DesignBook/DevHub) z release**, **compliance (privacy + usuwanie konta/danych)** oraz domknięcie smoke testów z `docs/PLAN.md`. Dodatkowo: **6‑cyfrowe kody** są **zgadywalne** i bez backendu nie da się ich sensownie zabezpieczyć w samych Firestore rules (brak rate limiting) — przy produkcji z danymi zdrowotnymi to duże ryzyko.

## Dlaczego 6‑cyfrowy kod jest problemem (konkretnie)
- 6 cyfr = 1 000 000 kombinacji.
- Kod jest dokumentem w `accessCodes/{code}` i aplikacja robi `get()` po dokumencie, żeby sprawdzić czy istnieje i potem go zużyć (`FirestoreCareLinkRepository.getLinkCodeInfo()` i `consumeLinkCode()`).
- W `firestore.rules` jest `allow get: if isSignedIn();` dla `match /accessCodes/{code}`, więc **każdy zalogowany** może próbować `get()` na losowych kodach aż trafi aktywny.
- Firestore rules nie mają mechanizmu “licz prób na minutę” → nie da się dodać twardej ochrony bez warstwy serwerowej albo dłuższego tokena.

Jeśli mimo tego zostajemy przy 6 cyfrach w produkcji, to w planie zapisuję to jako **świadomie zaakceptowane ryzyko**.

---

## 1) Blocker: Firebase w Release (żeby w ogóle działało na produkcji)
Aktualny stan:
- Nie ma `google-services.json` w repo (OK), ale też nie ma w ogóle pluginu `com.google.gms.google-services`, więc release nie dostanie konfiguracji “z automatu”.
- `SeniorApp.initFirebase()` ma fallback tylko dla `BuildConfig.DEBUG`, więc **release bez google-services** prawdopodobnie nie wstanie na produkcji.

Do zrobienia:
1. Dodać plugin `com.google.gms.google-services` (version catalog + `app/build.gradle.kts`).
2. Dostarczyć `app/google-services.json` dla produkcyjnego projektu Firebase (lokalnie/CI; nie musi być commitowany).
3. Smoke: `assembleRelease` + uruchomienie builda release na urządzeniu i pełny login SMS.

## 2) Blocker: Usunięcie debugowych “wejść” z wydania sklepowego
Aktualny stan:
- `DesignBookActivity` + `activity-alias` launcher są w `app/src/main/AndroidManifest.xml` (czyli trafią do release).
- W nawigacji jest `devHubFragment` i action z Role Select (`ui/src/main/res/navigation/nav_graph.xml`).

Do zrobienia:
1. Przenieść `DesignBookActivity` i `DesignBookLauncher` do `app/src/debug/AndroidManifest.xml` (albo gated przez flavor), żeby w release nie było drugiej ikony.
2. Zablokować `DevHubFragment` w release:
3. UI: brak jakiejkolwiek ścieżki do dev ekranów w buildzie release.

## 3) Compliance: Privacy policy + Data safety + “Usuwanie konta”
W repo nie ma śladów:
- polityki prywatności/ekranu informacyjnego,
- mechanizmu “Usuń konto”/“Usuń dane”,
- tekstu pod Data Safety (a aplikacja przetwarza dane zdrowotne + numer telefonu, opcjonalnie audio/mikrofon).

Do zrobienia (minimalny zestaw dla Play Production):
1. Dodać ekran w app: `Ustawienia -> Prywatność` z linkami:
2. Dodać “Usuń konto” w app (w UI pacjenta i opiekuna):
3. Przygotować URL do webowego “Delete account” (Play tego wymaga jako link w Console) spójny z tym, co robi app.
4. Przygotować treści do Data Safety (zdrowie, PII, audio/mikrofon, Firebase Auth/Firestore).

## 4) Bezpieczeństwo danych (minimalne hardening bez backendu)
Ponieważ wybrałeś “akceptujemy ryzyko” 6‑cyfrowych kodów:
- Zostawiamy mechanizm, ale wpisujemy w DoD MVP, że to jest **znane ryzyko**.

Minimalne rzeczy, które i tak warto dopiąć:
1. App Check (żeby utrudnić automatyczny scraping z “gołego” skryptu) — nie rozwiązuje brute-force w pełni, ale podnosi próg.
2. Bardzo krótkie TTL kodów + natychmiastowe kasowanie po użyciu (częściowo już jest).
3. Monitoring anomalii (min. logi po stronie Firebase / alerty), bo w release bez tego nie zobaczysz ataku.

## 5) Stabilność: domknięcie smoke testów z `docs/PLAN.md`
Na dziś są nieodhaczone m.in.:
- “Dane osobowe: smoke check fragmentu”
- “Dolegliwości: smoke check fragmentów”
- “10.12.4 Smoke: restart app (pacjent/opiekun) + utrzymanie kontekstu”
- “Walidacja: smoke test live update (pacjent/opiekun)”
- “Opiekun: badge/dashboards (smoke)”

Do zrobienia:
1. Wykonać te smoke testy na emulatorze i 1 fizycznym urządzeniu.
2. Wykonać te same scenariusze na buildzie release (najpierw Internal testing).

## 6) Release engineering (Play)
1. Dodać prawidłowe `signingConfig` pod upload key (albo przynajmniej pipeline pod AAB).
2. Zweryfikować wymagany `targetSdk` na Play na dzień 2026-02-24 i zaktualizować jeśli trzeba.
3. Utworzyć track `Internal testing` i tam wrzucić pierwszy AAB, zanim Production.

---

## Zmiany w publicznych interfejsach (planowane)
- `domain`: nowe use case’y typu `DeleteAccountUseCase`, `DeleteUserDataUseCase` (jeśli robimy “usuń dane bez usuwania auth”).
- `domain`: interfejs repo (np. `AccountRepository`) z implementacją w `data` (FirebaseAuth + Firestore).
- `ui`: nowe ekrany `PrivacyFragment` + `DeleteAccountFragment` (lub sekcja w istniejących ustawieniach).

## Testy i scenariusze akceptacyjne
- Release: instalacja + start bez emulatorów (`BuildConfig.USE_FIREBASE_*` false) i poprawna inicjalizacja Firebase.
- Auth: login pacjent i opiekun SMS na produkcyjnych usługach.
- Sesja: restart app → poprawne odtworzenie roli i aktywnego pacjenta.
- Realtime: pacjent dodaje pomiar → opiekun widzi update bez ręcznego refresh.
- Privacy: ekran prywatności dostępny, linki działają.
- Account deletion: usuwa dane użytkownika i konto (albo “konto + dane” zgodnie z polityką); po operacji app wraca do startu.

## Założenia (zamrożone na ten plan)
- Wydanie: Google Play **Production**.
- Backend danych: **Firestore produkcyjny**.
- Scope v1: **Pacjent + opiekun**.
- `DesignBookActivity` i dev narzędzia: **tylko debug**.
- 6‑cyfrowe kody linkowania: **zostają** i **ryzyko brute-force jest zaakceptowane** (odradzam, ale to jest decyzja wejściowa do planu).

## Etap 0 — System stylu (UI system)
- [x] Theme + kolory + typografia + shapes
- [x] Dimens (odstepy, rozmiary, promienie)
- [x] Style przyciskow (primary/secondary/danger)
- [x] Style kart i dialogow

## Etap 1 — Core, moduly, dependencje, DI, nawigacja
- [x] Moduly Gradle: app, domain, data, ui, di
- [x] Zaleznosci: RxJava/RxKotlin/RxAndroid, Koin, Timber, Navigation, Firebase
- [x] View Binding + Data Binding
- [x] Koin bootstrap + podstawowe moduly DI
- [x] Single-activity + Navigation Component (start flow)
- [x] Plan use case'ow (docs/USE_CASES.md)

## Etap 2 — Design Book Activity (dummy content)
- [x] Custom views (kafelki pomiarow, karty list)
- [x] List item pomiarow (custom view + layout)
- [x] Alerts overview (layout + custom view + demo)
- [x] Layout Design Book z pelnym zestawem komponentow
- [x] Activity + launcher alias
- [x] Wydzielenie wszystkich tekstow do resources (strings)

## Etap 3 — Porownanie z makietami (ADB/MCP)
- [x] Uruchomienie na emulatorze przez mcp__android-adb
- [x] Zrzuty ekranu Design Book
- [x] Optyczne porownanie z makietami
- [x] Checklista zgodnosci (layout/typografia/kolory/odstepy)

## Etap 4 — Pacjent: nawigacja + logika (use case + dummy repo)

Zasady etapu:
- Po dodaniu **pelnego ekranu**: uruchamiam ekran przez MCP i czekam na Twoja akceptacje.
- Bez samodzielnego porownywania do makiet i bez analizy obrazkow.
- Bez zapisu zrzutow ekranu do repo.
- Dopiero po akceptacji przechodzimy dalej.
- Kazdy ekran/flow commitowany osobno.

### 4.1 Pacjent flow (UI + nawigacja)
- [x] Nawigacja: kompletne wejscia/wyjscia, powroty
- [x] Ekran glowny pacjenta (kafelki pomiarow + akcje)
- [x] Dialog pomiaru: cukier
- [x] Dialog pomiaru: insulina
- [x] Dialog pomiaru: cisnienie
- [x] Dialog pomiaru: tetno
- [x] Historia pomiarow (wykres + lista + filtry)
- [x] Agenci monitorujacy: lista
- [x] Agenci monitorujacy: dodaj agenta (dialog)
- [x] Agenci monitorujacy: kod dostepu (dialog)
- [x] Agenci monitorujacy: edytuj agenta (dialog)
- [x] Agenci monitorujacy: dodaj lekarza (dialog)
- [x] Agenci monitorujacy: kod lekarza (dialog)
- [x] Agenci monitorujacy: edytuj lekarza (dialog)
- [x] Moje ustawienia (przeglad)
- [x] Dodaj dane osobowe (dialog)
- [x] Dodaj dolegliwość (dialog)
- [x] Dodaj lek (dialog)
- [x] Moje ustawienia: edycja dolegliwości/lekow/danych
- [x] Moje ustawienia: odswiezanie po edycji + QA
- [x] Dane osobowe: migracja dialog -> fragment (scroll)
- [x] Dane osobowe: VM/DI (Rx na Schedulers.io) + prefill przez observe
- [x] Dane osobowe: cleanup dialogu + usuniecie FragmentResult
- [ ] Dane osobowe: smoke check fragmentu + odswiezanie przez observe
- [x] Dolegliwości: migracja "Dodaj/Edytuj" do fragmentow (zamiast dialogow)
- [x] Dolegliwości: VM/DI dla add/edit (Rx na Schedulers.io) + powrot popBackStack
- [x] Dolegliwości: cleanup dialogow + usuniecie FragmentResult refresh
- [ ] Dolegliwości: smoke check fragmentow (dodaj/edytuj/usun) + odswiezanie listy przez observe
- [x] Alerty i powiadomienia (przeglad)
- [x] Alerty i powiadomienia (pelna konfiguracja)

### 4.2 Logika pacjenta (use case + repo dummy)
- [x] Pomiary: UC-MEAS-01..03 + dummy MeasurementRepository
- [x] Historia: UC-HIST-01..03 + dummy repo
- [x] Agenci i lekarze: UC-AGENT-01..07 + dummy AgentRepository/AccessCodeRepository/NotificationRepository
- [x] Alerty: UC-ALERT-01..07 + dummy AlertRepository
- [x] Ustawienia pacjenta: UC-SET-01..11 + dummy SettingsRepository
- [x] Integracja UI/VM z dummy use case
- [x] Podpiecie danych do widokow
- [x] Obsluga akcji dialogow (zapis/usun)
- [x] Listy dynamiczne (RecyclerView) w historii, agentach, ustawieniach i alertach
- [x] Walidacje formularzy w dialogach (wymagane pola)
- [x] Checklista UX dla MCP

## Etap 4.3 — Ustawienia: model danych osobowych
- [x] PersonalData: firstName/lastName + seed w InMemorySettingsRepository

## Etap 4.4 — Stabilizacja (MCP)
- [x] Fix: separator w subtitle alertow (brak "krzakow" typu `â€˘`)
- [x] Skrypt: smoke test MCP (ustawienia + alerty) na emulatorze

## Etap 4.5 — Powiadom agenta (dialog)
- [x] Domena + dummy powiadomienia (repo + UC + DI + USE_CASES)
- [x] Dialog UI + nawigacja (kafelek na Monitorze zdrowia)
- [x] ViewModel + logika wysylki + toast

## Etap 4.6 — Ustawienia: przebieg dolegliwości (opcje)
- [x] Aktualizacja opcji przebiegu dolegliwości (Lekki/Sredni/Ciezki/Bardzo ciezki) + seed + styl
- [x] Fix: dropdown przebiegu dolegliwości otwiera liste opcji
- [x] Reaktywne listy dolegliwości/lekow (observe + VM update)

## Etap 4.7 — Leki: nowy UX dodawania/edycji
- [x] Plan: MAIN-PLAN-dodawanie-leku + subplany
- [x] UI: dawka jako input, czestotliwosc 0..10, dynamiczne godziny
- [x] Logika: dynamiczne godziny + walidacja + zapis/prefill schedule
- [x] Migracja: "Dodaj lek" i "Edytuj lek" jako fragmenty ze scrollem (zamiast dialogow)
- [x] VM/DI: PatientAddMedViewModel + PatientEditMedViewModel (Rx na Schedulers.io)
- [x] Cleanup: usuniecie dialogow + usuniecie recznego refresh (FragmentResult)
- [ ] Walidacja: smoke check fragmentow (dodaj/edytuj/usun; 0/1/10) + odswiezanie listy przez observe

## Etap 5 — Opiekun + Wizyty (pelna funkcjonalnosc)

### 5.1 Opiekun flow (UI + nawigacja)
- [ ] Nawigacja: kompletne wejscia/wyjscia, powroty
- [ ] Panel opiekuna: dashboard
- [ ] Podopieczni: lista
- [ ] Podopieczni: dodaj (dialog)
- [ ] Podopieczni: kod dostepu (dialog)
- [ ] Wybierz podopiecznego

### 5.2 Wizyty (Pacjent + Opiekun)
- [ ] Wizyty pacjenta (lista)
- [ ] Wizyty pacjenta (dodaj/edytuj dialog)
- [ ] Wizyty opiekuna (lista)
- [ ] Wizyty opiekuna (dodaj/edytuj dialog)

### 5.3 Logika opiekuna i wizyt
- [ ] UC-CARE-01..08 + dummy CaregiverRepository
- [ ] UC-VISIT-01..06 + dummy VisitRepository
- [ ] UC-NOTIF-01..02 (powiadomienia wizyt) — mock

## Meta — Standard formularzy (dialog -> fragment)
- [x] `AGENTS.md`: standard budowy formularzy (scroll + VM + DI + observe, przyklad commita)
- [x] Skill: `formularz` (workflow + przyklad commita + szablon plikow)

## Etap 6 — Firebase Auth (SMS / numer telefonu)
- [x] Plan: subplany w `docs/plan/MAIN-PLAN-firebase-phone-auth.md`
- [x] Konfiguracja Firebase (deps + init + emulator w DEBUG)
- [x] Pacjent: logowanie SMS (wyslanie kodu + weryfikacja)
- [x] Opiekun: logowanie SMS (wyslanie kodu + weryfikacja) + ekran placeholder po zalogowaniu
- [x] Sesja: Role Select respektuje zalogowanie + logout (FirebaseAuth.signOut)
- [x] Walidacja: smoke test ADB/MCP na `emulator-5554` + instrukcja uruchomienia emulatora Auth

## Etap 7 — Firebase Firestore (emulator + przygotowanie pod realne dane)
- [x] Konfiguracja emulatora Firestore w DEBUG (`useEmulator(10.0.2.2, 8082)`)
- [x] Instrukcja uruchomienia emulatora Firestore
- [x] Plan: model danych Firestore (master + subplany w `docs/plan/MAIN-PLAN-firestore-user-data-model.md`)
- [x] Firestore: rules + indexes + wiring w `firebase.json`
- [x] Firestore: careLinks (pacjent↔opiekun) rules + indexy
- [x] Firestore: users/{uid} + bootstrap roli (PATIENT/CAREGIVER)
- [x] Migracja: SettingsRepository (InMemory -> Firestore)
- [x] Migracja: MeasurementRepository (InMemory -> Firestore)
- [x] Migracja: AgentRepository + AccessCodeRepository (InMemory -> Firestore)
- [x] Migracja: NotificationSettingsRepository (Firestore settings/notifications)
- [x] Migracja: DI przełączone na Firestore repozytoria

## Etap 8 — Realtime: PersonalData (watcher)
- [x] Domena + data + UI: observePersonalData (Firestore/InMemory) + ViewModel/Fragment
- [x] Docs: USE_CASES (UC-SET-12 ObservePersonalData)
- [ ] Walidacja: smoke test UI (odswiezanie po zapisie i po zewnetrznej zmianie)

## Etap 9 — Refaktor zgodnosci z AGENTS.md (architektura + UI)
- [x] 9.0 Aktualizacja AGENTS.md (Schedulers w VM) + plan refaktoru w PLAN.md
- [x] 9.1 API obserwacyjne: domain/data/di (Agents/Measurements/Alerts + use case observe)
- [ ] 9.2 Ekran: Patient Settings (overview + listy) — UI models + observe + data binding
- [x] 9.2.1 Patient Settings: UX (karta o mnie -> edycja, przyciski sekcji, usun konto)
- [ ] 9.3 Dialog: Personal Data (dodaj/edytuj)
- [ ] 9.4 Dialog: Disease add/edit
- [ ] 9.5 Dialog: Medication add/edit
- [ ] 9.6 Ekran: Patient Agents (lista)
- [ ] 9.7 Dialogi: Add/Edit Agent + Doctor
- [ ] 9.8 Dialogi: Access Code (agent/doctor)
- [ ] 9.9 Ekran: Patient History (lista + wykres)
- [ ] 9.10 Dialogi: Measurements (cukier/insulina/cisnienie/tetno)
- [x] 9.10.1 Measurements: voice input — retry (error 5/6/7) + 20s na start mowy
- [x] 9.10.2 Measurements: voice input — hint po ciszy + bezpieczny restart (cancel + delay)
- [x] 9.10.3 Measurements: voice input — spokojniejszy retry (backoff), mniej „pik”
- [x] 9.10.4 Measurements: voice input — bez retry na NO_MATCH (error 7) + bez retry dla pustych wynikow
- [ ] 9.11 Ekran: Patient Home (kafelki pomiarow)
- [x] 9.11.1 Patient Home: managed user state (domain/data)
- [x] 9.11.2 Patient Home: header UI (label/chevron + dane z Firestore)
- [x] 9.11.3 Patient Home: brak active patient -> CTA do CaregiverLink
- [ ] 9.11.4 Patient Home: smoke test (pacjent/opiekun)
- [x] 9.11.5 Patient Home: ukryj powiadomienia/pomoc + kafelki Historia/Ustawienia + wejscie do Agentow z Ustawien
- [x] 9.11.6 Patient Home: voice input — retry (error 5/6/7) + 20s na start mowy + hint po ciszy
- [x] 9.11.7 Patient Home: voice input — spokojniejszy retry (backoff), mniej „pik”
- [x] 9.11.8 Patient Home: voice input — bez retry na NO_MATCH (error 7) + bez retry dla pustych wynikow
- [x] 9.11.9 Patient Home: klik w panel profilu -> ustawienia
- [ ] 9.12 Ekran: Alerts Overview + Config
- [ ] 9.13 Auth: SMS verify (patient/caregiver) — VM + UI models + data binding
- [x] 9.14 Alerty i powiadomienia: plan (docs/plan) + wpis do PLAN.md
- [x] 9.15 Alerty i powiadomienia: nawigacja z ustawien -> config (bez overview)
- [x] 9.16 Alerty i powiadomienia: cleanup + spłaszczenie (1 ekran) + rename Config -> Alerts
- [x] 9.17 Alerty: chevron zwija/rozwija kartę konfiguracji (cukier)
- [x] 9.18 Alerty: dodaj konfiguracje (insulina/cisnienie/tetno) + animacje chevronu
- [x] 9.19 Alerty: plan cisnienie (SYS/DIA + kategorie ESC/ESH + alertEvents)
- [x] 9.20 Alerty: cisnienie — domena (klasyfikacja ESC/ESH + modele)
- [x] 9.21 Alerty: cisnienie — repo (Firestore/dummy) konfiguracji SYS/DIA + kategorie
- [x] 9.22 Alerty: alertEvents — domena/data + FirestorePaths
- [x] 9.23 Alerty: pomiar cisnienia -> generuj alertEvents (krytyczne/kategoria/spike + cooldown)
- [x] 9.24 Alerty: UI — konfiguracja cisnienia (pola + zapis + ObserveAlertConfig)
- [x] 9.25 Alerty: plan — reusable kanaly + opiekunowie per typ (bez progu kategorii)
- [x] 9.26 Alerty: cleanup — usun prog kategorii/cooldown (UI/domain/data/DI)
- [x] 9.27 Alerty: UI — reusable sekcja kanaly + opiekunowie (dla kazdego typu)
- [x] 9.28 Alerty: VM/DI — per typ kanaly + opiekunowie (ObserveAlertConfig + ObserveAgents)
- [x] 9.29 Alerty: alertEvents — ATTENTION po progach, CRITICAL po spike + klasyfikacja jako metadata
- [x] 9.30 Alerty: smoke — aktualizacja skryptu settings+alerts
- [x] 9.31 Alerty: cisnienie — domyslne progi (norma) + UI (hinty nie uciete)
- [x] 9.32 UI: input_height_compact — fix obcietych tekstow w polach
- [x] 9.33 Alerty: plan — config -> users/{uid}/settings/notifications (docs/plan) + wpis do PLAN.md
- [x] 9.34 Alerty: domain — NotificationSettings (settings usera) + use case'y
- [x] 9.35 Alerty: data — Firestore repo configu w users/{uid}/settings/notifications
- [x] 9.36 Alerty: DI/UI — przepiecie na NotificationSettings + cleanup starego configu
- [x] 9.37 Alerty: walidacja — assembleDebug + test
- [x] 9.38 Alerty: docs — aktualizacja USE_CASES + Firestore schema (settings/notifications)
- [x] 9.39 Alerty: fix — update pol per-typ bez nadpisywania mapy alerts
- [x] 9.40 Alerty: cleanup — usuniecie legacy /alerts i migracji
- [x] 9.41 Alerty: UI — ids + copy (cukier spadek/okno) + bez przycisku zapisu
- [x] 9.42 Alerty: domain/data — sugar drop + pressure bez min/max
- [x] 9.43 Alerty: auto-save — pressure/sugar/pulse (debounce)
- [x] 9.44 Alerty: walidacja — assembleDebug
- [x] 9.45 Alerty: docs — pressure bez min/max + sugar drop fields
- [x] 9.46 Alerty: defaults — seed channels + progi w Firestore
- [x] 9.47 Alerty: walidacja — assembleDebug
- [x] 9.48 Alerty: defaults — progi cukru/tetna (normy)
- [x] 9.49 Alerty: seed defaults po rejestracji (personalData + notifications)
- [x] 9.50 CareLink: auto-dodanie opiekuna do kontaktow pacjenta
- [x] 9.51 CareLink: transaction reads before writes (fix)
- [x] 9.52 CareLink: opiekun po loginie (kolejka, bez transakcji)
- [x] 9.53 CareLink: sync danych profilu opiekuna do contacts pacjentow
- [x] 9.54 Ustawienia: sekcje Moi lekarze/Moi opiekunowie + listy i przyciski
- [x] 9.55 Lekarze: dodaj/edytuj jako fragmenty + VM (formularz)
- [x] 9.56 Opiekunowie: dodaj/edytuj jako fragmenty + kod powiazania na add
- [x] 9.57 Nawigacja: usuniecie ekranu Agenci + routing do formularzy
- [x] 9.58 Cleanup: usuniecie dialogow/kodow Agenci monitorujacy
- [x] 9.59 Walidacja: assembleDebug (smoke manual formularzy — pending)
- [x] 9.60 CareLink: kody dwukierunkowe bez wymogu numeru docelowego (repo + rules)
- [x] 9.61 Pacjent: Dodaj opiekuna — utworz kod + wpisz kod na jednym ekranie
- [x] 9.62 Opiekun: Dodaj podopiecznego — wpisz kod + generuj kod bez telefonu
- [x] 9.63 Walidacja: assembleDebug po zmianach kodow laczenia
- [x] 9.64 Hotfix: brak crasha Rx (Undeliverable InterruptedException) przy wpisaniu kodu podopiecznego
- [x] 9.65 Hotfix: SwitchCompat NPE (null text) — showText=false dla switchy w alertach/opiekunie/lekach
- [x] 9.66 Hotfix: SwitchCompat NPE — app:showText + globalny switchStyle fallback (showText/textOn/textOff)
- [x] 9.67 CareLink: symetryczne tworzenie kontaktu pacjenta po consume kodu
- [x] 9.68 CareLink: cleanup VM i DI (single source of truth linkowania)
- [x] 9.69 CareLink: walidacja assembleDebug po zmianach linkowania

## Etap 10 — Opiekun: link + ActivePatient (test realtime)
- [x] 10.1 Domena: careLinks + konteksty (ActivePatient, CurrentRole) + use case
 - [x] 10.2 Data: FirestoreCareLinkRepository + kody linkowania
 - [x] 10.3 Data: PatientUidProvider + Firestore repo na patientUid
- [x] 10.4 DI: wiring careLinks + konteksty + repo
- [x] 10.5 UI: CaregiverLinkFragment + nawigacja do Patient flow
- [x] 10.6 UI: wejście pacjenta do linkowania + zapis roli po loginie
- [x] 10.7 Hotfix: opiekun po loginie -> PatientHome (bez link screen)
- [x] 10.8 Hotfix: RoleSelect (zalogowany) -> Monitor Zdrowia
- [x] 10.9 Hotfix: login opiekuna z kodem pacjenta -> auto-link + Monitor
- [x] 10.10 Hotfix: careLinks get bez null error w rules
- [x] 10.11 Hotfix: careLinks get allow for exists()

## Etap 10.12 — Sesja: persist roli/pacjenta + startup gate + realtime home
- [x] 10.12.1 Persist: rola + aktywny pacjent (SharedPrefs) + logout cleanup
- [x] 10.12.2 Startup gate: auto-route + recovery roli z Firestore
- [x] 10.12.3 Monitor zdrowia: realtime ostatnie pomiary (Observable)
- [ ] 10.12.4 Smoke: restart app (pacjent/opiekun) + utrzymanie kontekstu

## Etap 11 — Synchronizacja/obserwowanie pomiarow
- [x] Plan: MAIN-PLAN-synchronizacja-obserwowanie-pomiarow
- [x] Observe: PatientHistory (lista + wykres) na Observable
- [x] Fix: obserwacje niezalezne od filtrow (listen all + local filter)
- [x] Cleanup: usuniecie history_refresh + notify w dialogach
- [ ] Walidacja: smoke test live update (pacjent/opiekun)

## Etap 12 — Opiekun: Panel (taby) + Podopieczni
- [x] Panel opiekuna: routing (login + startup gate) + nav_graph
- [x] Panel opiekuna: UI tabs (TabLayout + ViewPager2) + placeholdery
- [x] Podopieczni: lista careLinks (kafelki) + wybór pacjenta
- [x] Podopieczni: wybór pacjenta po kliknięciu całego kafelka
- [x] Podopieczni: formularz dodaj + kod + kopiuj
- [x] Auth pacjenta: login z kodem (consume careLink + prefill danych)
- [x] Auth pacjenta: auto-SMS po kodzie (bez wpisywania telefonu)
- [x] Auth pacjenta: draft w accessCodeDrafts + rules (wrazliwe dane po phone-auth)
- [x] Podopieczni: powiaz pacjenta kodem w formularzu
- [x] Monitor zdrowia: powrot do wyboru pacjenta (ikona wyjscia)
- [x] Monitor zdrowia: exit door jak system back (powrot do podopieczni)
- [x] Opiekun: badge nowych pomiarow (domain/data/DI)
- [x] Opiekun: badge nowych pomiarow (UI/VM)
- [ ] Opiekun: badge nowych pomiarow (smoke)
- [x] Opiekun: dashboard — nowe pomiary (domain/data/DI)
- [x] Opiekun: dashboard — nowe pomiary (UI/VM)
- [x] Opiekun: dashboard — agregacja pomiarow wszystkich podopiecznych
- [ ] Opiekun: dashboard — nowe pomiary (smoke)
- [x] Opiekun: blokada caregiver↔caregiver + reguly profilu opiekuna
- [x] Opiekun: "Ja / moj profil" w Podopiecznych + personalData seed
- [x] Pacjent: opiekunowie z profilu (observe agents)
- [x] Pacjent: agenci monitorujacy (observe agents)

## Etap 13 — Speech to Text (monitor zdrowia)
- [x] STT: plan (docs/plan/MAIN-PLAN-obsluga-speech-to-text.md)
- [x] STT: parser domenowy (voice parse)
- [x] STT: Patient Home — wprowadzanie glosowe + zapis
- [x] STT: Patient Home — indikator nagrywania + okienko tekstu
- [x] STT: Patient Home — badge w miejscu tytulu + ring + dluzsze sluchanie
- [x] STT: dialogi pomiarow — wskazniki + wspolny layout voice
 - [x] STT: dialogi pomiarow — ukryj mic w edycji
 - [x] STT: wskaznik nagrywania trzyma do finalnego wyniku
 - [x] STT: Patient Home — 10s okno ciszy na kolejne pomiary
 - [x] STT: dialogi pomiarow — 10s okno ciszy
 - [x] STT: wydluz start nasluchu (restart timeout)

## Etap 13.1 — Terminologia i UI
- [x] Terminologia: dolegliwosci + usuniecie "Lista lekow" w ustawieniach pacjenta

## Etap 14 — Leki: przypomnienia lokalne + opiekun
- [x] 14.1 Domain/Data: eventy przypomnien + read state + prefs opiekuna + repo/use case
- [x] 14.2 Android: alarmy + receiver + notyfikacje lokalne (pacjent + opiekun)
- [x] 14.3 Coordinator: obserwacje lekow/prefs i planowanie alarmow
- [x] 14.4 UI: toggle per podopieczny + dashboard eventy lekow + badge
- [x] 14.7 Potwierdzenie "Wzialem lek": akcja w powiadomieniu pacjenta + sync statusu w Firestore + dashboard/push opiekuna (local)
- [x] 14.8 UI: kafelek podopiecznego — switch "Powiadamiaj o lekach" (bez duplikacji tekstu)
- [x] 14.8.1 Hotfix: kafelek podopiecznego — render switcha "Powiadamiaj o lekach" (SwitchMaterial)
- [ ] 14.5 QA: smoke (pacjent/opiekun)
- [ ] 14.6 FCM: push do opiekunow na event przypomnienia leku
  - [ ] 14.6.1 Domain/Data: rejestracja tokenow FCM per user + UC + DI
  - [ ] 14.6.2 Firebase: przechowywanie tokenow + reguly dostepu
  - [ ] 14.6.3 Cloud Functions: trigger na event przypomnienia -> wysylka push do opiekunow
  - [ ] 14.6.4 Logika: uwzglednij preferencje opiekuna (toggle) przy wysylce
  - [ ] 14.6.5 QA: test push na urzadzeniu fizycznym (opiekun)

## Etap 15 — UI: spójne marginesy i spacing list/grid
- [x] 15.1 UI core: semantyczne dimeny spacingu + wspolne ItemDecoration (list/grid)
- [ ] 15.2 Opiekun Dashboard: usuniecie wrapper-card i podwojnych marginesow + spacing listy
- [ ] 15.3 Opiekun Podopieczni: spacing grid bez marginow w itemie (ItemDecoration)
- [ ] 15.4 Rollout: listy pacjenta/alertow na ItemDecoration (bez marginTop w itemach)
- [ ] 15.5 QA: smoke spacing (dashboard/podopieczni/monitor ustawienia/historia)

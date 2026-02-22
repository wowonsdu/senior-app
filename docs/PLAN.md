# Plan wykonania (checklista)

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
- [x] Dodaj chorobe (dialog)
- [x] Dodaj lek (dialog)
- [x] Moje ustawienia: edycja chorob/lekow/danych
- [x] Moje ustawienia: odswiezanie po edycji + QA
- [x] Dane osobowe: migracja dialog -> fragment (scroll)
- [x] Dane osobowe: VM/DI (Rx na Schedulers.io) + prefill przez observe
- [x] Dane osobowe: cleanup dialogu + usuniecie FragmentResult
- [ ] Dane osobowe: smoke check fragmentu + odswiezanie przez observe
- [x] Choroby: migracja "Dodaj/Edytuj" do fragmentow (zamiast dialogow)
- [x] Choroby: VM/DI dla add/edit (Rx na Schedulers.io) + powrot popBackStack
- [x] Choroby: cleanup dialogow + usuniecie FragmentResult refresh
- [ ] Choroby: smoke check fragmentow (dodaj/edytuj/usun) + odswiezanie listy przez observe
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

## Etap 4.6 — Ustawienia: przebieg choroby (opcje)
- [x] Aktualizacja opcji przebiegu choroby (Lekki/Sredni/Ciezki/Bardzo ciezki) + seed + styl
- [x] Fix: dropdown przebiegu choroby otwiera liste opcji
- [x] Reaktywne listy chorob/lekow (observe + VM update)

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
- [x] Migracja: AlertRepository (InMemory -> Firestore)
- [x] Migracja: DI przełączone na Firestore repozytoria

## Etap 8 — Realtime: PersonalData (watcher)
- [x] Domena + data + UI: observePersonalData (Firestore/InMemory) + ViewModel/Fragment
- [x] Docs: USE_CASES (UC-SET-12 ObservePersonalData)
- [ ] Walidacja: smoke test UI (odswiezanie po zapisie i po zewnetrznej zmianie)

## Etap 9 — Refaktor zgodnosci z AGENTS.md (architektura + UI)
- [x] 9.0 Aktualizacja AGENTS.md (Schedulers w VM) + plan refaktoru w PLAN.md
- [x] 9.1 API obserwacyjne: domain/data/di (Agents/Measurements/Alerts + use case observe)
- [ ] 9.2 Ekran: Patient Settings (overview + listy) — UI models + observe + data binding
- [ ] 9.3 Dialog: Personal Data (dodaj/edytuj)
- [ ] 9.4 Dialog: Disease add/edit
- [ ] 9.5 Dialog: Medication add/edit
- [ ] 9.6 Ekran: Patient Agents (lista)
- [ ] 9.7 Dialogi: Add/Edit Agent + Doctor
- [ ] 9.8 Dialogi: Access Code (agent/doctor)
- [ ] 9.9 Ekran: Patient History (lista + wykres)
- [ ] 9.10 Dialogi: Measurements (cukier/insulina/cisnienie/tetno)
- [ ] 9.11 Ekran: Patient Home (kafelki pomiarow)
- [x] 9.11.1 Patient Home: managed user state (domain/data)
- [x] 9.11.2 Patient Home: header UI (label/chevron + dane z Firestore)
- [x] 9.11.3 Patient Home: brak active patient -> CTA do CaregiverLink
- [ ] 9.11.4 Patient Home: smoke test (pacjent/opiekun)
- [x] 9.11.5 Patient Home: ukryj powiadomienia/pomoc + kafelki Historia/Ustawienia + wejscie do Agentow z Ustawien
- [ ] 9.12 Ekran: Alerts Overview + Config
- [ ] 9.13 Auth: SMS verify (patient/caregiver) — VM + UI models + data binding
- [x] 9.14 Alerty i powiadomienia: plan (docs/plan) + wpis do PLAN.md
- [x] 9.15 Alerty i powiadomienia: nawigacja z ustawien -> config (bez overview)
- [x] 9.16 Alerty i powiadomienia: cleanup + spłaszczenie (1 ekran) + rename Config -> Alerts
- [x] 9.17 Alerty: chevron zwija/rozwija kartę konfiguracji (cukier)
- [x] 9.18 Alerty: dodaj konfiguracje (insulina/cisnienie/tetno) + animacje chevronu

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
- [x] Podopieczni: formularz dodaj + kod + kopiuj
- [x] Auth pacjenta: login z kodem (consume careLink + prefill danych)
- [x] Auth pacjenta: auto-SMS po kodzie (bez wpisywania telefonu)
- [x] Auth pacjenta: draft w accessCodeDrafts + rules (wrazliwe dane po phone-auth)
- [x] Podopieczni: powiaz pacjenta kodem w formularzu
- [x] Monitor zdrowia: powrot do wyboru pacjenta (ikona wyjscia)
- [x] Monitor zdrowia: exit door jak system back (powrot do podopieczni)
- [x] Opiekun: badge nowych pomiarow (domain/data/DI)
- [ ] Opiekun: badge nowych pomiarow (UI/VM)
- [ ] Opiekun: badge nowych pomiarow (smoke)
- [x] Opiekun: dashboard — nowe pomiary (domain/data/DI)
- [x] Opiekun: dashboard — nowe pomiary (UI/VM)
- [ ] Opiekun: dashboard — nowe pomiary (smoke)

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

## Etap 6 — Firebase Auth (SMS / numer telefonu)
- [x] Plan: subplany w `docs/plan/MAIN-PLAN-firebase-phone-auth.md`
- [x] Konfiguracja Firebase (deps + init + emulator w DEBUG)
- [x] Pacjent: logowanie SMS (wyslanie kodu + weryfikacja)
- [ ] Opiekun: logowanie SMS (wyslanie kodu + weryfikacja) + ekran placeholder po zalogowaniu
- [ ] Sesja: Role Select respektuje zalogowanie + logout (FirebaseAuth.signOut)
- [ ] Walidacja: smoke test ADB/MCP na `emulator-5554` + instrukcja uruchomienia emulatora Auth

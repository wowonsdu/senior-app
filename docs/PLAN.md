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

## Etap 4 — Implementacja flow (Pacjent -> Opiekun -> Logika)

Zasady etapu:
- Po dodaniu **pelnego ekranu**: uruchamiam ekran przez MCP i czekam na Twoja akceptacje.
- Bez samodzielnego porownywania do makiet i bez analizy obrazkow.
- Bez zapisu zrzutow ekranu do repo.
- Dopiero po akceptacji przechodzimy dalej.
- Kazdy ekran/flow commitowany osobno.

### 4.1 Pacjent flow (UI + nawigacja)
- [ ] Nawigacja: kompletne wejscia/wyjscia, powroty
- [x] Ekran glowny pacjenta (kafelki pomiarow + akcje)
- [x] Dialog pomiaru: cukier
- [x] Dialog pomiaru: insulina
- [x] Dialog pomiaru: cisnienie
- [x] Dialog pomiaru: tetno
- [ ] Historia pomiarow (wykres + lista + filtry)
- [ ] Agenci monitorujacy: lista
- [ ] Agenci monitorujacy: dodaj agenta (dialog)
- [ ] Agenci monitorujacy: kod dostepu (dialog)
- [ ] Agenci monitorujacy: edytuj agenta (dialog)
- [ ] Agenci monitorujacy: dodaj lekarza (dialog)
- [ ] Agenci monitorujacy: kod lekarza (dialog)
- [ ] Agenci monitorujacy: edytuj lekarza (dialog)
- [ ] Moje ustawienia (przeglad)
- [ ] Dodaj dane osobowe (dialog)
- [ ] Dodaj chorobe (dialog)
- [ ] Dodaj lek (dialog)
- [ ] Alerty i powiadomienia (przeglad)
- [ ] Alerty i powiadomienia (pelna konfiguracja)
- [ ] Wizyty pacjenta (lista)
- [ ] Wizyty pacjenta (dodaj/edytuj dialog)

### 4.2 Opiekun flow (UI + nawigacja) — pozniej
- [ ] Nawigacja: kompletne wejscia/wyjscia, powroty
- [ ] Panel opiekuna: dashboard
- [ ] Podopieczni: lista
- [ ] Podopieczni: dodaj (dialog)
- [ ] Podopieczni: kod dostepu (dialog)
- [ ] Wybierz podopiecznego
- [ ] Wizyty opiekuna (lista)
- [ ] Wizyty opiekuna (dodaj/edytuj dialog)

### 4.3 Logika (use case + repo + Firebase) — pozniej
- [ ] Dummy repo (mock danych zgodnych z makietami)
- [ ] Use case logika (dummy)
- [ ] Firebase repo (Auth + Firestore)

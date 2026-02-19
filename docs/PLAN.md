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

## Etap 4 — Implementacja flow (Auth -> Pacjent -> Opiekun)

Zasady etapu:
- Po dodaniu **pelnego ekranu**: weryfikacja z makieta przez MCP.
- Bez nowych screenshotow w repo.
- Dopiero po akceptacji przechodzimy dalej.
- Kazdy ekran/flow commitowany osobno.

### 4.1 Auth flow
- [x] Nawigacja: kompletne wejscia/wyjscia, powroty
- [ ] Layouty i widoki — Ekran wyboru roli
- [ ] Layouty i widoki — Logowanie (telefon/kod) — pacjent
- [ ] Layouty i widoki — Logowanie (telefon/kod) — opiekun
- [ ] Layouty i widoki — Weryfikacja SMS — pacjent
- [ ] Layouty i widoki — Weryfikacja SMS — opiekun
- [ ] Dummy repo (mock danych zgodnych z makietami)
- [ ] Use case logika (dummy)
- [ ] Firebase repo (Auth + docelowe storage)

### 4.2 Pacjent flow
- [ ] Nawigacja: kompletne wejscia/wyjscia, powroty
- [ ] Layouty i widoki (zgodne z makietami)
- [ ] Dummy repo (mock danych zgodnych z makietami)
- [ ] Use case logika (dummy)
- [ ] Firebase repo (Firestore)

### 4.3 Opiekun flow
- [ ] Nawigacja: kompletne wejscia/wyjscia, powroty
- [ ] Layouty i widoki (zgodne z makietami)
- [ ] Dummy repo (mock danych zgodnych z makietami)
- [ ] Use case logika (dummy)
- [ ] Firebase repo (Firestore)

# Alerty i powiadomienia: spłaszczenie — Nawigacja + cleanup

## Scope
- Docelowo jeden ekran „Alerty i powiadomienia” (bez osobnego overview).
- Wejście z `PatientSettingsFragment` ma otwierać od razu ekran konfiguracji.
- W tym etapie robimy tylko sprzątanie nawigacji/layoutów/nazw oraz usuwamy duplikację ekranów.

## Out of scope
- Auto-zapis (zapis w momencie zmiany pól/przełączników).
- Logika rozwijania sekcji (chevron expand/collapse) i dopinanie konfiguracji dla pozostałych typów.

## Modules and layers
- UI: fragmenty, layouty, `strings.xml`, `nav_graph.xml`
- DI: tylko aktualizacje referencji po rename (jeśli potrzebne)
- Domain/Data: bez zmian

## Deliverables
- Jeden fragment docelowy: `PatientAlertsFragment` + `fragment_patient_alerts.xml`
- Brak osobnych: `PatientAlertsOverviewFragment` / `fragment_patient_alerts_overview.xml`
- `nav_graph.xml`: jedno destination dla alertów, wejście z ustawień bezpośrednio
- Uporządkowane stringi tytułu/subtitle dla jednego ekranu

## Implementation checklist
- [ ] `docs/PLAN.md`: dodać kroki 9.14–9.16 + odhaczać po wykonaniu
- [ ] `docs/plan`: dodać master plan + subplan (ten plik)
- [ ] Nawigacja: `PatientSettingsFragment` -> ekran alertów (bez overview)
- [ ] NavGraph: usunąć overview destination i action overview->config
- [ ] Rename: `PatientAlertsConfigFragment` -> `PatientAlertsFragment`
- [ ] Rename: `fragment_patient_alerts_config.xml` -> `fragment_patient_alerts.xml` + binding IDs
- [ ] Cleanup: usunąć nieużywane pliki (overview fragment/layout, adapter jeśli zbędny)
- [ ] `strings.xml`: ujednolicić nazwy i użycie tytułu/subtitle ekranu
- [ ] `fragment_patient_settings.xml`: karta alertów ma title + subtitle

## Validation
- [ ] Build: `./gradlew :ui:assembleDebug`
- [ ] Smoke: z ustawień pacjenta wejście w „Alerty i powiadomienia” otwiera ekran config i wraca backiem bez crashy


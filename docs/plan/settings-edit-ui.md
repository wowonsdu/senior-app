# Etap 2 - Edycja UI (dolegliwości/leki/dane) Plan

## Scope
- Tap na elementach listy dolegliwości/lekow otwiera dialog edycji.
- Tap na karcie danych osobowych otwiera dialog edycji.
- Dialogi edycji dolegliwości/lekow maja przycisk "Usun".
- UI dostosowane do nowego modelu `PersonalData` (firstName/lastName).

## Modules And Layers
- ui
- navigation

## Deliverables
- `PatientEditDiseaseFragment` + layout.
- `PatientEditMedFragment` + layout.
- Aktualizacja `PatientPersonalDataFragment` do trybu edycji i prefill.
- Aktualizacja `PatientSettingsUiState` i `PatientSettingsFragment` do nowego modelu.
- Zmiany w adapterach (klik w item).
- Akcje nawigacji z argumentami `diseaseId` / `medicationId`.

## Implementation Checklist
- [x] Dodac klik w itemach dolegliwości i lekow w adapterach.
- [x] Ustawic `MaterialCardView` w itemach jako clickable, jesli potrzeba.
- [x] Dodac akcje i argumenty w `nav_graph.xml` dla edycji (id dolegliwości/leku).
- [x] Dodac nowe dialogi edycji z prefill z repo (pobranie listy i wybor po id).
- [x] Dodac "Usun" w dialogach edycji dolegliwości/lekow i obsluzyc `Remove*UseCase`.
- [x] Ustawic tap na `patient_settings_personal` jako wejscie do edycji danych osobowych.
- [x] Zaktualizowac `PatientSettingsUiState` oraz `PatientSettingsFragment` do `firstName/lastName`.
- [x] Zaktualizowac `PatientPersonalDataFragment` (prefill + zapis `firstName/lastName`).
- [x] Zaktualizowac stringi tytulow/CTA ("Edytuj dolegliwość", "Edytuj lek", "Usun").
- [x] Zaktualizowac `docs/PLAN.md` i zrobic commit opisujacy etap.

## Validation
- [x] Tap na itemach listy otwiera edycje z prefill.
- [x] "Usun" usuwa wpis i nie crashuje.
- [x] Po zapisie dane sa widoczne w liscie.
- [x] Karta danych osobowych pokazuje poprawne imie i nazwisko.

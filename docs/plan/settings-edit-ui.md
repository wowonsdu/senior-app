# Etap 2 � Edycja UI (choroby/leki/dane) Plan

## Scope
- Tap na elementach listy chorob/lekow otwiera dialog edycji.
- Tap na karcie danych osobowych otwiera dialog edycji.
- Dialogi edycji chorob/lekow maja przycisk "Usun".
- UI dostosowane do nowego modelu `PersonalData` (firstName/lastName).

## Modules And Layers
- ui
- navigation

## Deliverables
- `PatientEditDiseaseDialogFragment` + layout.
- `PatientEditMedDialogFragment` + layout.
- Aktualizacja `PatientPersonalDataDialogFragment` do trybu edycji i prefill.
- Aktualizacja `PatientSettingsUiState` i `PatientSettingsFragment` do nowego modelu.
- Zmiany w adapterach (klik w item).
- Akcje nawigacji z argumentami `diseaseId` / `medicationId`.

## Implementation Checklist
- [x] Dodac klik w itemach chorob i lekow w adapterach.
- [x] Ustawic `MaterialCardView` w itemach jako clickable, jesli potrzeba.
- [x] Dodac akcje i argumenty w `nav_graph.xml` dla edycji (id choroby/leku).
- [x] Dodac nowe dialogi edycji z prefill z repo (pobranie listy i wybor po id).
- [x] Dodac "Usun" w dialogach edycji chorob/lekow i obsluzyc `Remove*UseCase`.
- [x] Ustawic tap na `patient_settings_personal` jako wejscie do edycji danych osobowych.
- [x] Zaktualizowac `PatientSettingsUiState` oraz `PatientSettingsFragment` do `firstName/lastName`.
- [x] Zaktualizowac `PatientPersonalDataDialogFragment` (prefill + zapis `firstName/lastName`).
- [x] Zaktualizowac stringi tytulow/CTA ("Edytuj chorobe", "Edytuj lek", "Usun").
- [x] Zaktualizowac `docs/PLAN.md` i zrobic commit opisujacy etap.

## Validation
- [ ] Tap na itemach listy otwiera edycje z prefill.
- [ ] "Usun" usuwa wpis i nie crashuje.
- [ ] Po zapisie dane sa widoczne w liscie.
- [ ] Karta danych osobowych pokazuje poprawne imie i nazwisko.

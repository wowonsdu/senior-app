# Leki — Fragmenty: nawigacja i szkielety — Plan

## Scope
- Zastepujemy `PatientAddMedDialogFragment` i `PatientEditMedDialogFragment` pelnoekranowymi fragmentami.
- Formularze musza byc scrollowalne (dynamiczne godziny 0..10).

## Modules And Layers
- ui (nawigacja, fragmenty, layouty)

## Deliverables
- Nowe fragmenty i layouty:
  - `PatientAddMedFragment`
  - `PatientEditMedFragment`
  - `fragment_patient_add_med.xml`
  - `fragment_patient_edit_med.xml`
- Aktualizacja `nav_graph.xml`: add/edit med jako `<fragment>` zamiast `<dialog>`.

## Implementation Checklist
- [ ] Dodac destinations `<fragment>` dla add/edit med w `ui/src/main/res/navigation/nav_graph.xml`.
- [ ] Zmienic akcje z `PatientSettingsFragment` na nowe destinations (bez zmiany action id).
- [ ] Dodac nowe layouty fragmentow z toolbar + `ScrollView` / `NestedScrollView`.
- [ ] Dodac klasy fragmentow z podstawowym bindingiem i obsluga back.

## Validation
- [ ] Z `Moje ustawienia` -> `Dodaj lek` otwiera sie nowy ekran (scroll).
- [ ] Klik w element listy lekow otwiera `Edytuj lek` jako ekran (scroll).


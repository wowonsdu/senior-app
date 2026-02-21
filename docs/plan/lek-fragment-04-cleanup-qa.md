# Leki — Cleanup + QA — Plan

## Scope
- Usuniecie starych dialogow (nieuzywane po migracji do fragmentow).
- Usuniecie recznego odswiezania settings przez FragmentResult (oparte o observe).
- Smoke check calego flow.

## Modules And Layers
- ui + docs

## Deliverables
- Brak referencji do `PatientAddMedDialogFragment` / `PatientEditMedDialogFragment`.
- `PatientSettingsFragment` bez `FragmentResultListener` dla odswiezania.
- Zaktualizowany `docs/PLAN.md` (odhaczone kroki).

## Implementation Checklist
- [ ] Usunac dialog destinations z `nav_graph.xml`.
- [ ] Usunac klasy dialogow i ich layouty (jesli nieuzywane).
- [ ] Usunac FragmentResultListener z `PatientSettingsFragment`.
- [ ] Smoke check: dodaj/edytuj/usun; 0/1/10; lista aktualizuje sie przez observe.

## Validation
- [ ] Reczne testy na emulatorze / uruchomienie aplikacji bez crashy.


# Leki — Fragment "Edytuj/Usun lek" — Plan

## Scope
- Formularz edycji leku jako fragment.
- Prefill na podstawie `medicationId` (reaktywnie).
- Update i delete przez ViewModel (Rx na `Schedulers.io`).

## Modules And Layers
- ui + di

## Deliverables
- `PatientEditMedViewModel` w Koin.
- Prefill: name/dose/notify + czestotliwosc + godziny z `schedule` (CSV).
- Usuwanie z potwierdzeniem.

## Implementation Checklist
- [ ] Dodac `PatientEditMedViewModel` (observe + update + remove).
- [ ] Podpiac VM w `DiModules.kt` (viewModelModule).
- [ ] Prefill formularza na podstawie `medicationId`.
- [ ] Update: walidacja jak w dodawaniu, zapis CSV, popBackStack.
- [ ] Delete: potwierdzenie (MaterialAlertDialogBuilder) + remove + popBackStack.

## Validation
- [ ] Edycja leku prefilluje dane i pozwala zapisac zmiany.
- [ ] Usuniecie usuwa wpis i po powrocie nie ma go na liscie.


# Leki — Fragment "Dodaj lek" — Plan

## Scope
- Formularz dodawania leku jako fragment.
- Dynamiczne godziny wg "Ile razy dziennie" (0..10).
- Zapis przez ViewModel + `AddMedicationUseCase` na `Schedulers.io`.

## Modules And Layers
- ui + di

## Deliverables
- `PatientAddMedViewModel` w Koin.
- Zapis draftu: `MedicationDraft(name, dosage, scheduleCsv, notificationsEnabled)`.
- Powrot do ustawien po sukcesie (popBackStack).

## Implementation Checklist
- [ ] Dodac `PatientAddMedViewModel` (Rx subscribeOn io + observeOn main).
- [ ] Podpiac VM w `DiModules.kt` (viewModelModule).
- [ ] W fragmencie: dropdown czestotliwosci + render N pol godzin.
- [ ] Walidacja: name/dose/frequency; godziny wymagane gdy frequency > 0.
- [ ] Zapis: `schedule` jako CSV godzin, dla 0 -> pusty string.

## Validation
- [ ] Dodaj lek z 0: zapisuje pusty schedule.
- [ ] Dodaj lek z 3: wymaga 3 godzin i zapisuje CSV.
- [ ] Po zapisie lista lekow aktualizuje sie przez `observeMedications` (bez recznego refresh).


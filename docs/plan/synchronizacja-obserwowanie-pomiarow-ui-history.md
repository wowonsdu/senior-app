# Synchronizacja/Obserwowanie Pomiarow — UI History

## Scope
- Historia pomiarow (lista + wykres) ma byc oparta o Observable.
- Usuniecie recznego odswiezania historii (FragmentResult + notify z dialogow).

## Modules And Layers
- ui
- di

## Deliverables
- `PatientHistoryViewModel` pracuje na obserwacjach.
- `PatientHistoryFragment` nie nasluchuje `history_refresh`.
- Dialogi pomiarow nie wysylaja manualnego refreshu.

## Implementation Checklist
- [ ] Zmien `PatientHistoryViewModel` na `ObserveMeasurementHistoryUseCase`.
- [ ] Zmien `PatientHistoryViewModel` na `ObserveMeasurementChartDataUseCase`.
- [ ] Dodaj/zarzadzaj `Disposable` dla obserwacji (reset przy zmianie filtrow).
- [ ] Usun `FragmentResult` "history_refresh" z `PatientHistoryFragment`.
- [ ] Usun `notifyHistoryChangedAndDismiss()` z dialogow pomiarow.
- [ ] Zaktualizuj DI dla `PatientHistoryViewModel`.

## Validation
- [ ] Aplikacja kompiluje sie po zmianach UI/DI.

# Synchronizacja/Obserwowanie Pomiarow — Observe Domain/Data

## Scope
- Weryfikacja obserwacji historii i wykresu pomiarow w warstwie data/domain.

## Modules And Layers
- data
- domain

## Deliverables
- Potwierdzenie, ze obserwacje korzystaja z query snapshot i emitują dane na zmiany.

## Implementation Checklist
- [ ] Sprawdz `FirestoreMeasurementRepository.observeMeasurementHistory()`.
- [ ] Sprawdz `FirestoreMeasurementRepository.observeMeasurementChartData()`.
- [ ] Potwierdz gotowosc `ObserveMeasurementHistoryUseCase` i `ObserveMeasurementChartDataUseCase`.

## Validation
- [ ] Brak zmian wymaganych poza UI (lub minimalne poprawki, jesli wyjdą).

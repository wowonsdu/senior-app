# Etap 1 - Model danych osobowych Plan

## Scope
- Zastapienie `fullName` w `PersonalData` polami `firstName` i `lastName`.
- Aktualizacja warstw domain/data do nowego modelu (bez zmian w UI).

## Modules And Layers
- domain
- data

## Deliverables
- Zaktualizowany `PersonalData` i jego uzycia w domain/data.
- Zmienione seedy w `InMemorySettingsRepository`.

## Implementation Checklist
- [x] Zmienic `PersonalData` na `firstName` + `lastName`.
- [x] Zaktualizowac `SettingsRepository.getPersonalData()` i `upsertPersonalData()`.
- [x] Zaktualizowac `GetSettingsOverviewUseCase` jesli wymagane.
- [x] Zaktualizowac `InMemorySettingsRepository` i dane seed.
- [x] Zaktualizowac `docs/PLAN.md` i zrobic commit opisujacy etap.

## Validation
- [x] Brak crashy w `GetSettingsOverviewUseCase`.
- [x] Dane w repo zwracaja `firstName/lastName` zgodnie z seedingiem.


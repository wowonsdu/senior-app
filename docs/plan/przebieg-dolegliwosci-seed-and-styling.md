# Seed + Stylowanie Przebiegu Dolegliwości — Plan

## Scope
- Aktualizacja seed danych w InMemory.
- Dostosowanie mapowania stylu dla nowych etykiet przebiegu.

## Modules And Layers
- data
- ui

## Deliverables
- Seed z nowymi wartosciami przebiegu.
- Zaktualizowane mapowanie stylu chipow.

## Implementation Checklist
- [ ] Zmienic seed przebiegu w `InMemorySettingsRepository` na Lekki/Sredni/Ciezki/Bardzo ciezki.
- [ ] Rozszerzyc `PatientDiseasesAdapter.resolveSeverityStyle` o rozpoznawanie "Bardzo ciezki" i "Lekki".
- [ ] Nie migrowac istniejacych danych Firestore.

## Validation
- [ ] Sprawdzic widok listy dolegliwości dla seed (chip + kolor).

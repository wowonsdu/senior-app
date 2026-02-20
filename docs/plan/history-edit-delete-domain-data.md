# Domain + Data Plan

## Scope
- Dodanie update/delete dla pomiarow w repozytoriach i use case'ach.

## Modules And Layers
- domain
- data
- di

## Deliverables
- Nowe metody w MeasurementRepository
- Use case'y update/delete
- Implementacja Firestore + InMemory
- Rejestracja w Koin

## Implementation Checklist
- [ ] Rozszerz MeasurementRepository o update/delete
- [ ] Dodaj UpdateMeasurementUseCase i UpdateBloodPressureMeasurementUseCase
- [ ] Dodaj DeleteMeasurementUseCase
- [ ] Zaimplementuj update/delete w FirestoreMeasurementRepository
- [ ] Zaimplementuj update/delete w InMemoryMeasurementRepository
- [ ] Zarejestruj use case'y w module DI

## Validation
- [ ] Build compile

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
- [x] Rozszerz MeasurementRepository o update/delete
- [x] Dodaj UpdateMeasurementUseCase i UpdateBloodPressureMeasurementUseCase
- [x] Dodaj DeleteMeasurementUseCase
- [x] Zaimplementuj update/delete w FirestoreMeasurementRepository
- [x] Zaimplementuj update/delete w InMemoryMeasurementRepository
- [x] Zarejestruj use case'y w module DI

## Validation
- [ ] Build compile

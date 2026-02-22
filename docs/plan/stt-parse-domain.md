# STT Parse Domain Plan

## Scope
- Logika parsowania tekstu STT na listê pomiarów.

## Modules And Layers
- Domain

## Deliverables
- domain/src/main/kotlin/zdrowy/senior/io/domain/measurement/ParsedVoiceMeasurement.kt
- domain/src/main/kotlin/zdrowy/senior/io/domain/measurement/VoiceParseResult.kt
- domain/src/main/kotlin/zdrowy/senior/io/domain/measurement/ParseVoiceMeasurementsUseCase.kt
- di/src/main/kotlin/zdrowy/senior/io/di/DiModules.kt

## Implementation Checklist
- [ ] Zdefiniuj ParsedVoiceMeasurement (type + value/systolic/diastolic).
- [ ] VoiceParseResult z measurements + skippedTypes.
- [ ] ParseVoiceMeasurementsUseCase:
- [ ] Normalizacja tekstu (lowercase + bez znakow diakrytycznych).
- [ ] Wykrywanie slow kluczowych i segmentacja.
- [ ] Ekstrakcja liczb (regex) i walidacja.
- [ ] Pomijanie niepelnych pomiarow + oznaczenie skippedTypes.
- [ ] Rejestracja use case w domainModule.

## Validation
- [ ] (Opcjonalnie) testy jednostkowe domeny.

# Firestore — patient measurements plan

## Scope
- Mapowanie `MeasurementRepository` na Firestore + zapytania pod recent/history/chart.

## Modules And Layers
- domain: `MeasurementRepository`
- data: `FirestoreMeasurementRepository`

## Deliverables
- `users/{patientUid}/measurements/*` zgodnie z domeną.
- Query shapes: recent/history/chart.

## Implementation Checklist
- [ ] addMeasurement (value) i addBloodPressureMeasurement (systolic/diastolic).
- [ ] recent: orderBy(timestampMs desc) + limit + opcjonalny whereIn(type).
- [ ] history: zakres dat + opcjonalny whereIn(type) + orderBy(timestampMs).
- [ ] chart: pobranie jak history + agregacja client-side.

## Validation
- [ ] Emulator: dodaj pomiar → widoczny na recent i w historii.


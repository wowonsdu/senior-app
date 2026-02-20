# Firestore — patient alerts plan

## Scope
- Mapowanie `AlertRepository` na dokumenty per-typ pomiaru.

## Modules And Layers
- domain: `AlertRepository`, `AlertSetting`, `AlertChannel`
- data: `FirestoreAlertRepository`

## Deliverables
- `users/{patientUid}/alerts/{measurementType}` (1 doc per typ).
- Update’y cząstkowe bez potrzeby przepisywania całej konfiguracji.

## Implementation Checklist
- [ ] getAlertConfig(): złożyć `AlertConfig` dla wszystkich typów (fallback do defaultów).
- [ ] setAlertEnabled/updateCriticalThresholds/updateSpikeRules/updateAlertChannels/updateAlertCaregivers → update jednego doc.

## Validation
- [ ] Emulator: zmiana jednego typu nie nadpisuje innych.


# Firestore — canonical schema plan

## Scope
- Kanoniczne ścieżki, nazwy pól, ID dokumentów i mapowania do modeli domeny.
- Query shapes pod UI pacjenta oraz pod przyszłe Stage 5 (opiekun, wizyty).

## Modules And Layers
- `domain/*` — modele i interfejsy repozytoriów
- `data/*` — implementacje Firestore repozytoriów
- `di/*` — przełączanie InMemory/Firestore

## Deliverables
- Spis kolekcji/dokumentów/pól (+ typy).
- Kontrakt ID: które dokumenty mają `uid` jako ID, które `autoId`.
- Kontrakt sortowania i filtrów list.

## Implementation Checklist
- [ ] `users/{uid}`: `role`, `schemaVersion`, `createdAt`, `lastLoginAt`, `phoneNumberE164`, `displayName`.
- [ ] Pacjent: `settings/personalData`, `diseases/*`, `medications/*`.
- [ ] Pacjent: `measurements/*` (`timestampMs`, `type`, `value/systolic/diastolic`, `source`).
- [ ] Pacjent: `contacts/*` (CAREGEIVER/DOCTOR + `linkedUid?`).
- [ ] Pacjent: `alerts/{measurementType}` (enabled/min/max/spike/window/channels/caregiverUids).
- [ ] Opiekun: `careLinks` po obu stronach (denormalizacja).
- [ ] Kody: `accessCodes/{code}` (TTL, consumedAt, patientUid, contactId).

## Validation
- [ ] Spójność z `domain/*Repository.kt` (wszystkie operacje pokryte).


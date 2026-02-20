# Firestore — contacts + careLinks + accessCodes plan

## Scope
- Kontakty pacjenta (opiekun/lekarz) jako hybryda: mogą nie mieć konta, opcjonalnie `linkedUid`.
- Powiązania opiekun↔pacjent (`careLinks`) jako 2-stronna denormalizacja.
- Kody dostępu jako top-level `accessCodes/{code}`.

## Modules And Layers
- domain: `AgentRepository`, `AccessCodeRepository`
- data: `FirestoreAgentRepository`, `FirestoreAccessCodeRepository`

## Deliverables
- `users/{patientUid}/contacts/*` CRUD.
- `accessCodes/{code}` create (generate).
- (Stage 5) consume kodu + tworzenie careLinks w transakcji.

## Implementation Checklist
- [ ] `contacts/*`: add/update/remove/list (CAREGEIVER/DOCTOR + specialization).
- [ ] `linkedUid` jako opcjonalne pole na kontakcie.
- [ ] `accessCodes/{code}`: TTL (expiresAtMs), patientUid, contactId.

## Validation
- [ ] Emulator: pacjent generuje kod → dokument istnieje.


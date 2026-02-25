# Zabezpieczenie logowania — 03 — Domain/Data

Cel:
Dopasowac kontrakty repozytoriow i use case do nowych wymagan (phoneNumberE164) oraz usunac sciezki, ktore umozliwiaja odczyt kodu bez phone gate.

## Domain
- [x] Ustalic, czy CareLinkCode / AccessCode zawieraja phoneNumberE164
- [x] Doprecyzowac UseCase:
  - GenerateLinkCodeUseCase / GenerateAccessCodeForAgentUseCase: wymagany phoneNumberE164 dla kodow "do wpisania"
  - GetLinkCodeInfoUseCase / ConsumeLinkCodeUseCase: zaklada, ze user jest po Phone Auth

## Data (Firestore)
- [x] FirestoreCareLinkRepository:
  - getLinkCodeInfo / consumeLinkCode: pracuja na accessCodes z phoneNumberE164
- [x] FirestoreAccessCodeRepository (dla agentow, jesli to tez "do wpisania"):
  - decyzja: agent-kody nie przechodza na phone gate; dostep ograniczony po patientUid w rules

## Checklist
- [x] Spisac mapowanie pol i kompatybilnosc danych (jeśli istnieja stare kody w bazie)
- [x] Usunac/wylaczyc stare wejscia i nav actions, ktore pozwalaja na kod bez SMS
- [x] Implementacja: accessCodes pod docId = phoneE164, kod w polu `code`

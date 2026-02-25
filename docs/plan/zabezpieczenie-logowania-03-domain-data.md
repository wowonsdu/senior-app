# Zabezpieczenie logowania — 03 — Domain/Data

Cel:
Dopasowac kontrakty repozytoriow i use case do nowych wymagan (phoneNumberE164) oraz usunac sciezki, ktore umozliwiaja odczyt kodu bez phone gate.

## Domain
- [ ] Ustalic, czy CareLinkCode / AccessCode zawieraja phoneNumberE164
- [ ] Doprecyzowac UseCase:
  - GenerateLinkCodeUseCase / GenerateAccessCodeForAgentUseCase: wymagany phoneNumberE164 dla kodow "do wpisania"
  - GetLinkCodeInfoUseCase / ConsumeLinkCodeUseCase: zaklada, ze user jest po Phone Auth

## Data (Firestore)
- [ ] FirestoreCareLinkRepository:
  - getLinkCodeInfo / consumeLinkCode: pracuja na accessCodes z phoneNumberE164
- [ ] FirestoreAccessCodeRepository (dla agentow, jesli to tez "do wpisania"):
  - decyzja: czy agent-kody tez przechodza na phone gate, czy zostaja long token / inny mechanizm

## Checklist
- [ ] Spisac mapowanie pol i kompatybilnosc danych (jeśli istnieja stare kody w bazie)
- [ ] Usunac/wylaczyc stare wejscia i nav actions, ktore pozwalaja na kod bez SMS


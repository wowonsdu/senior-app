# Execute Master Plan — Zabezpieczenie logowania

Context:
Wdrażasz zabezpieczenie logowania / linkowania oparte o weryfikacje numeru telefonu (Firebase Phone Auth),
a dopiero potem wpisywanie 6-cyfrowego kodu. Celem jest zablokowanie brute-force na accessCodes w Firestore
bez backendu (Cloud Functions) i bez nietypowych rules.

Plan file: docs/plan/MAIN-PLAN-zabezpieczenie-logowania.md

Instructions:
1. Otworz master plan i zacznij od Etapu 0 (zamroz decyzje PATIENT->CAREGIVER).
2. Wykonuj subplany po kolei.
3. Po kazdym ukonczonym kroku: odhacz checkbox w planie i zrob commit opisujacy krok.
4. Na koniec uruchom smoke scenariusze z subplanu QA.

## Parallelizacja (bez kolizji)
- Agent A: model + rules (docs/plan/zabezpieczenie-logowania-01-model-rules.md)
- Agent B: UI flow (docs/plan/zabezpieczenie-logowania-02-ui-flow.md)
- Agent C: domain/data (docs/plan/zabezpieczenie-logowania-03-domain-data.md)
- Agent D: QA/smoke (docs/plan/zabezpieczenie-logowania-04-qa-smoke.md) po A/B/C

## Subplan Prompts
- [ ] docs/plan/prompts/PROMPT-zabezpieczenie-logowania-01-model-rules.md
- [ ] docs/plan/prompts/PROMPT-zabezpieczenie-logowania-02-ui-flow.md
- [ ] docs/plan/prompts/PROMPT-zabezpieczenie-logowania-03-domain-data.md
- [ ] docs/plan/prompts/PROMPT-zabezpieczenie-logowania-04-qa-smoke.md


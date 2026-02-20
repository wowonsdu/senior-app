# Execute Master Plan — Moje ustawienia: edycja danych

Context:
Implementujesz edycjê danych osobowych, chorób i leków w ekranie "Moje ustawienia". U¿yj master planu, aby realizowaæ prace etapami i aktualizowaæ checklisty.

Plan file: docs/plan/MAIN-PLAN-my-settings-edit.md

Instructions:
1. Otwórz master plan i zidentyfikuj aktualny etap.
2. Otwórz powi¹zany subplan i wykonuj zadania po kolei.
3. Aktualizuj checkboxy w planach po zakoñczeniu zadañ.
4. Podsumuj postêpy i blokery.

## Parallelizacja (bez kolizji)
- Agent A: PROMPT-settings-edit-personal-model.md (domain/data tylko).
- Agent B: PROMPT-settings-edit-ui.md (ui/navigation tylko).
- Agent C: PROMPT-settings-edit-qa.md po A i B.

## Subplan Prompts
- [ ] docs/plan/prompts/PROMPT-settings-edit-personal-model.md
- [ ] docs/plan/prompts/PROMPT-settings-edit-ui.md
- [ ] docs/plan/prompts/PROMPT-settings-edit-qa.md

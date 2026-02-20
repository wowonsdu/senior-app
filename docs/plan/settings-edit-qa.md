# Etap 3 — Odswiezanie stanu + QA Plan

## Scope
- Odswiezanie danych w "Moje ustawienia" po zapisie i usunieciu.
- Smoke testy UI.

## Modules And Layers
- ui
- domain

## Deliverables
- Spojne odswiezanie list po dismiss dialogow.
- Checklista QA.

## Implementation Checklist
- [x] Ustalic odswiezanie: `viewModel.load()` w `onResume` lub fragment result po dialogu.
- [x] Upewnic sie, ze lista i dane osobowe aktualizuja sie po edycji.
- [x] Wykonac smoke testy (wejscie w edycje, zapis, usuniecie).
- [x] Zaktualizowac `docs/PLAN.md` i zrobic commit opisujacy etap.

## Validation
- [x] Edycja/Usuniecie odswieza dane w widoku.
- [x] Brak regresji w innych sekcjach ustawien.
- [x] Zmiana danych osobowych odswieza karte "O mnie".


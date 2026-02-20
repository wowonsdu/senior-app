# Plan - Edycja w "Moje ustawienia"

## Summary
- Dodanie edycji danych osobowych, chorob i lekow z poziomu ekranu "Moje ustawienia".
- Wejscie do edycji: tap w element listy (choroby/leki) oraz tap na karcie danych osobowych.
- Dialogi edycji chorob i lekow zawieraja przycisk "Usun".
- Model `PersonalData` zmieniony na `firstName` + `lastName` (zamiast `fullName`).

## Assumptions
- Brak dodatkowego potwierdzenia usuniecia (zgodnie z dotychczasowym wzorcem dialogow w projekcie).
- Id elementu przekazywany do dialogu edycji przez argumenty nawigacji.
- Odzwiezanie danych po zamknieciu dialogow odbywa sie przez `viewModel.load()` (np. w `onResume`).

## API / Model Changes
- `PersonalData` zawiera pola `firstName` i `lastName` zamiast `fullName`.
- UI sklada pelne imie z `firstName` i `lastName`.

## Podzial na agentow (bez kolizji)
- Agent A: Etap 1 (tylko domain/data).
- Agent B: Etap 2 (tylko ui/navigation/strings/layout).
- Agent C: Etap 3 (ui) po zakonczeniu A i B.

## Etap 1 - Model danych osobowych
- [x] Zmiana modelu `PersonalData` na `firstName/lastName` i aktualizacja warstw domain/data
- [x] Subplan: docs/plan/settings-edit-personal-model.md

## Etap 2 - Edycja UI (choroby/leki/dane)
- [x] Wejscia do edycji + dialogi edycji + nawigacja
- [x] Subplan: docs/plan/settings-edit-ui.md

## Etap 3 - Odswiezanie stanu + QA
- [x] Odswiezanie list po zapisie/usunieciu i smoke testy
- [x] Subplan: docs/plan/settings-edit-qa.md


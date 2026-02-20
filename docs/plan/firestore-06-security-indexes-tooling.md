# Firestore — security rules, indexes, emulator tooling plan

## Scope
- MVP reguł dostępu + indexy pod zapytania.
- Instrukcje uruchomienia i debugging (missing index / permission denied).

## Modules And Layers
- tooling: Firebase emulator, `firestore.rules`, `firestore.indexes.json`, `firebase.json`
- data: zapytania wymagające indeksów

## Deliverables
- `firestore.rules` + `firestore.indexes.json` i wiring w `firebase.json`.

## Implementation Checklist
- [ ] Reguły: pełny dostęp do `users/{uid}/**` tylko dla właściciela (`request.auth.uid == uid`).
- [ ] Index: `measurements` (type + timestampMs) pod whereIn + orderBy.
- [ ] Spisać krótko jak sprawdzić błędy w Emulator UI.

## Validation
- [ ] Emulator: pacjent czyta/pisze swoje dane.


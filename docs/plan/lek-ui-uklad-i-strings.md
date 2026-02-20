# Leki — UI: uklad i strings — Plan

## Scope
- Zmiana ukladu dialogow dodawania/edycji leku.
- Aktualizacja zasobow tekstowych i list wyboru.

## Modules And Layers
- UI (layouty, strings)

## Deliverables
- Dawka jako zwykly input (bez dropdown).
- Czestotliwosc jako dropdown 0..10.
- Dynamiczna sekcja godzin przyjecia.

## Implementation Checklist
- [ ] Zamienic pole "Dawka" na TextInputEditText (bez endIcon dropdown).
- [ ] Dodac dropdown "Ile razy dziennie" z wartosciami 0..10.
- [ ] Zastapic statyczne 3 pola godzin kontenerem dynamicznym.
- [ ] Zaktualizowac strings (hinty i array czestotliwosci).

## Validation
- [ ] Sprawdzic layout obu dialogow (add/edit) wizualnie.

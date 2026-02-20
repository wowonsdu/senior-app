# Leki — Logika dialogow — Plan

## Scope
- Dynamiczne pola godzin w oparciu o czestotliwosc.
- Walidacja i zapis harmonogramu.
- Prefill w edycji leku.

## Modules And Layers
- UI (fragmenty dialogow)

## Deliverables
- Logika generowania N pol godzin.
- Walidacja zgodna z nowymi zasadami.
- Zapis `schedule` jako CSV godzin.

## Implementation Checklist
- [ ] Dodac adapter dla dropdownu czestotliwosci (0..10) + showDropDown.
- [ ] Renderowanie N pol godzin po wyborze czestotliwosci.
- [ ] Walidacja: nazwa, dawka, czestotliwosc; godziny wymagane gdy >0.
- [ ] Zapis `schedule` jako CSV godzin; dla 0 -> pusty string.
- [ ] Prefill edycji: parsowanie schedule i ustawienie pol.

## Validation
- [ ] Sprawdzic dodanie i edycje leku z rozna liczba godzin.

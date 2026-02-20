# Dialogi Edycji Pomiarow Plan

## Scope
- Edycja/usuwanie pomiaru w istniejacych dialogach (cukier/insulina/cisnienie/tetno).

## Modules And Layers
- ui
- domain
- di

## Deliverables
- Argumenty edycji w nav_graph
- Widoczne przyciski "Usun" w dialogach
- Prefill pol i zapis/usun przez use case'y

## Implementation Checklist
- [ ] Dodaj argumenty edycji do dialogow w nav_graph
- [ ] Dodaj przycisk Usun do layoutow dialogow
- [ ] Uzyj wspolnego ViewModela dialogow (Koin)
- [ ] Prefill pol z argumentow
- [ ] Save -> update, Delete -> delete
- [ ] Emituj refresh listy historii po zapisie/usunieciu

## Validation
- [ ] Otworz dialog z historii i sprawdz prefill
- [ ] Zapis i usun dzialaja

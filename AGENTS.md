# AGENTS

## Zasady pracy
- Po kazdym ukonczonym kroku z `docs/PLAN.md` aktualizuj checklistê (odhacz) i zrob commit.
- Commit powinien opisywac wykonany krok (np. "Complete UI style system step").
- Nie pomijaj aktualizacji `docs/PLAN.md`.

## Architektura i przeplyw danych
- Pracujesz nad aplikacja Android w Clean Architecture, z DI w Koin.
- Dane i repozytoria sa zawsze w module `data`.
- Cala logika jest w `domain` jako pojedyncze use case'y z operatorem `invoke`.
- Use case'y uruchamiaj zawsze asynchronicznie przez RxKotlin na `Schedulers.io`.
- Mapowanie na modele domenowe odbywa sie w ViewModelach.
- UI (fragmenty/aktywnoœci) dostaja juz gotowe, zmapowane modele.

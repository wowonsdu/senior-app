# AGENTS

## Zasady pracy
- Po kazdym ukonczonym kroku z `docs/PLAN.md` aktualizuj checklistę (odhacz) i zrob commit.
- Commit powinien opisywac wykonany krok (np. "Complete UI style system step").
- Nie pomijaj aktualizacji `docs/PLAN.md`.

## Architektura i przeplyw danych
- Pracujesz nad aplikacja Android w Clean Architecture, z DI w Koin.
- Dane i repozytoria sa zawsze w module `data`.
- Cala logika jest w `domain` jako pojedyncze use case'y z operatorem `invoke`.
- Use case'y uruchamiaj zawsze asynchronicznie przez RxKotlin na `Schedulers.io`.
- Mapowanie na modele domenowe odbywa sie w ViewModelach.
- UI (fragmenty/aktywności) dostaja juz gotowe, zmapowane modele.
- ViewModele zawsze inicjalizuj w module Koin (viewModelModule), bez Factory.

## Planowanie i flow init-plan
- Gdy uzytkownik zawoła `$init-plan`, najpierw zapytaj, czy jest w trybie planowania; kontynuuj dopiero po potwierdzeniu.
- Gdy uzytkownik uruchamia `init-plan`, najpierw popros o tytul ficzera do planowania.
- Po tytule popros o zalozenia i input do planu.
- Wejdz w tryb planowania i wykonaj analize, plan oraz pytania doprecyzowujace.
- Po przygotowaniu glownego planu zapytaj, czy rozbic go na subplany (male kawalki).
- Zapytaj, czy wdrozyc plan.
- Jesli uzytkownik potwierdzi wdrozenie, uruchom `init-plan` i podziel plan na subplany zgodnie ze skillem.
- Po zapisaniu planow zapytaj, czy przygotowac prompty i czy przydzielic agentow.
- Przed przydzialem agentow zaproponuj ich liczbe i zakres pracy, a nastepnie popros o potwierdzenie.
- Po potwierdzeniu wygeneruj prompty przez `init-prompts`, zapisz wszystko w `/docs` i wdrazaj wg promptow.



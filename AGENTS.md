# AGENTS

## Zasady pracy
- Po kazdym ukonczonym kroku z `docs/PLAN.md` aktualizuj checklistę (odhacz) i zrob commit.
- Commit powinien opisywac wykonany krok (np. "Complete UI style system step").
- Nie pomijaj aktualizacji `docs/PLAN.md`.

## Architektura i przeplyw danych
- Pracujesz nad aplikacja Android w Clean Architecture z MVVM, z DI w Koin.
- Podzial na moduly: `ui`, `domain`, `data`, `di`.
- Dane i repozytoria sa zawsze w module `data`.
- Cala logika jest w `domain` jako pojedyncze use case'y z operatorem `invoke`.
- Use case'y uruchamiaj zawsze asynchronicznie przez RxKotlin na `Schedulers.io` w ViewModelach.
- Mapowanie na modele widokowe odbywa sie w ViewModelach.
- UI (fragmenty/aktywności) dostaja juz gotowe, zmapowane modele.
- ViewModele zawsze inicjalizuj w module Koin (viewModelModule), bez Factory.

## UI
- Data binding i view binding.
- Widoki czyste, pozbawione logiki, oparte o MVVM.
- ViewModele wstrzykuja domenowe use case'y z `operator fun invoke()`.
- Obsluga klikniec i przekazywanie danych zmapowanych na modele widokowe w ViewModelach.
- UI nie zna domeny, a domena nie zna widokow.
- Minimalna ilosc logiki w ViewModelu, wszystko co logicznie uzasadnione ma byc w use case.
- Asynchronicznosc oparta o RxKotlin: `Single`, `Observable`, `Completable`, `Maybe`.
- Nie rob nic na glownym watku poza aktualizacja UI.

## Domain
- Wystawia interfejsy repozytoriow implementowanych w `data`.
- Wystawia modele domenowe i zawiera cala logike.
- ViewModele spina ja z UI i mapuja, wyjatek to ficzery Androidowe, ktorych domena nie obsluguje.

## Data
- Dostarcza implementacje przez repozytoria oraz warstwe bazy danych i serwisow.
- Domena nie wie nic o bazie danych ani serwisach.

## DI
- Spina wszystkie moduly i dostarcza zaleznosci przez Koin.
- Wstrzykuje ViewModele, use case'y, repozytoria i pozostale zaleznosci w odpowiedniej kolejnosci.

## Reaktywnosc i Firestore
- Aplikacja bazuje na Firestore Database.
- Wszystko, co wymienia dane, ma byc spiete jako `Observable` na query.
- Kazdy widok obserwuje zmiany danych, nawet jesli edycja odbywa sie w dialogu.
- Aktualizacja danych ma wynikac z obserwowania query, nie z recznego przekazywania miedzy dialogiem a widokiem.

## Formularze (standard)
Cel: kazdy formularz, ktory moze miec wiecej pol / dynamiczne sekcje, robimy jako pelnoekranowy fragment ze scrollem (zamiast `DialogFragment`).

### UI (fragment)
- Toolbar (`MaterialToolbar`) z back + tytul/subtitle.
- `ScrollView` / `NestedScrollView` z `fillViewport=true`, a w srodku `LinearLayout` z polami.
- Na dole sekcja akcji: `Anuluj`/`Zapisz` (add) oraz `Usun`/`Zapisz` (edit).
- Walidacja w UI: ustawiaj `TextInputLayout.error` dla wymaganych pol.

### ViewModel (logika)
- VM zawiera tylko spinanie use case'ow i walidacje UI-level (wymagane pola); business rules w `domain`.
- Use case'y uruchamiaj zawsze: `subscribeOn(Schedulers.io())` + `observeOn(AndroidSchedulers.mainThread())`.
- Na sukces: nawigacja `popBackStack()` (bez `FragmentResult`).
- Prefill w edycji: reaktwnie przez `ObserveXxxUseCase()` i wybor po `id` (lista -> element).

### Nawigacja + cleanup
- `nav_graph.xml`: zamien `<dialog>` na `<fragment>` i przepnij `action_*` na nowe destination IDs.
- Usun stare dialogi i ich layouty (zeby nie dublowac UX).
- Nie dodawaj recznego odswiezania list (FragmentResult / manual refresh). Lista ma sie aktualizowac przez obserwacje (`ObserveXxxUseCase`).

### Commit (przyklad)
- Przykład (implementacja ekranow + VM): `Choroby: dodaj/edytuj jako fragmenty + VM`
- Przykład (nawigacja): `Choroby: nawigacja do fragmentow`
- Przykład (cleanup): `Choroby: cleanup dialogow i FragmentResult`

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



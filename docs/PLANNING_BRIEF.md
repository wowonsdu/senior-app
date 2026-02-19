# Brief planistyczny – Aplikacja Monitorowania Zdrowia dla Seniorow (Android)

## Cel produktu
Zbudowac natywna aplikacje Android dla seniorow do monitorowania zdrowia, z prostym wprowadzaniem pomiarow, historia danych, alertami i wspolpraca z opiekunami/lekarzami. Aplikacja ma byc czytelna, prosta i bezpieczna, z danymi przechowywanymi lokalnie. Integracje z Firebase (Auth/Firestore) maja byc przygotowane, ale na start dopuszczalne sa implementacje in-memory.

## Grupy uzytkownikow
- Pacjent (senior): rejestruje pomiary, przeglada historie, zarzadza lekami/chorobami, ustawia alerty i opiekunow.
- Opiekun/Lekarz: monitoruje podopiecznych, przeglada pomiary i historie, planuje wizyty.

## Kluczowe funkcjonalnosci
1) System pomiarow zdrowotnych
- Typy: glukoza, insulina, cisnienie (skurczowe/rozkurczowe), tetno.
- Wprowadzanie: duze przyciski 2x2 na ekranie glownym, wpisywanie reczne (klawiatura numeryczna), wprowadzanie glosowe (Speech-to-Text).
- Kazdy pomiar zapisywany z data i godzina.

2) Historia pomiarow
- Wykresy liniowe z osia czasu i tooltipami.
- Lista pomiarow (chronologicznie, kolorami, ikony, wartosci, data/godzina).
- Filtry typow pomiarow oraz zakres czasu.

3) Zarzadzanie agentami (opiekunowie i lekarze)
- Dodawanie agenta/lekarza: imie i nazwisko, rola/relacja, telefon, email, specjalizacja (lekarz).
- Generowanie 6-cyfrowego kodu dostepu, kopiowanie i wysylka SMS.
- Lista, edycja i usuwanie agentow; wizualne rozroznienie ról.

4) Panel opiekuna
- Logowanie kodem lub SMS.
- Lista podopiecznych i wybor osoby do podgladu.
- Dashboard: nowe pomiary i zmiany trendow.
- Dostep do historii pomiarow i danych podopiecznego.

5) Alerty i powiadomienia
- Progi krytyczne dla kazdego typu pomiaru.
- Wykrywanie gwaltownych zmian (procent/okno czasowe).
- Przypisanie opiekunow do alertow.
- Kanaly: SMS, email, powiadomienia w aplikacji.

6) Wizyty
- Typy: wizyta opiekuna u pacjenta / pacjent u lekarza.
- Dodawanie: typ, data/godzina, lekarz (opcjonalnie), notatki.
- Powiadomienia o wizycie (SMS i w aplikacji) + czas wyprzedzenia.
- Lista wizyt: nadchodzace/wszystkie/zakonczone, edycja i oznaczanie statusu.

7) Ustawienia (Moje ustawienia)
- Dane osobowe pacjenta.
- Moi opiekunowie.
- Moje choroby: dodawanie/edycja/usuwanie, stopien ciezkosci.
- Moje leki: dawka, godziny przyjec, powiadomienia.
- Konfiguracja alertow jako osobna sekcja.

## Mapa ekranow (skrot)
Pacjent:
- Start: wybor roli.
- Logowanie pacjenta (telefon/kod) + weryfikacja SMS.
- Ekran glowny pacjenta (kafelki pomiarow + szybkie akcje).
- Dialogi pomiarow: cukier, insulina, cisnienie, tetno.
- Historia pomiarow: widoki z filtrami i wykresami.
- Agenci monitorujacy: lista, dodawanie, edycja, kody dostepu.
- Ustawienia: dane osobowe, choroby, leki, alerty.
- Alerty i powiadomienia: przeglad + pelna konfiguracja.

Opiekun:
- Logowanie opiekuna (telefon/kod) + weryfikacja SMS.
- Panel opiekuna: dashboard, podopieczni, wizyty.
- Wybierz podopiecznego + dodaj podopiecznego (dialog + kod SMS).
- Wizyty: planowanie, lista, statusy (nadchodzace/wszystkie/zakonczone).

## Plan use case'ow (skrot)
Szczegolowa lista w `docs/USE_CASES.md`. Najwazniejsze obszary:
- Auth i sesja: SMS, kod dostepu, logowanie/wylogowanie.
- Pomiary: dodawanie, historia, wykresy.
- Agenci i lekarze: CRUD, generowanie kodu, wysylka SMS.
- Opiekun: podopieczni, dashboard, oznaczanie nowych pomiarow.
- Alerty: progi, kanaly, przypisanie opiekunow, zapis konfiguracji.
- Wizyty: dodawanie, edycja, statusy i przypomnienia.
- Ustawienia pacjenta: dane, choroby, leki i powiadomienia.

## Zasady realizacji ekranow
- Po dodaniu pelnego ekranu: weryfikacja z makieta przez MCP.
- Zapis zrzutu ekranu do `docs/screens` (nazwy z podkreslnikami).
- Dopiero po akceptacji przechodzimy dalej.
- Kazdy ekran/flow commitowany osobno.

## UX i wymagania wizualne
- Material Design (Material 3 w estetyce UI).
- Duze przyciski i czytelne karty (dla seniorow).
- Wysoki kontrast, proste układy, minimalizm.

## Dane i prywatnosc
- Dane przechowywane lokalnie na urzadzeniu.
- Brak serwera jako wymog startowy.
- Dostep opiekuna/lekarza przez kody dostepu.

## Platforma i technologia
- Android, Kotlin.
- UI: XML + Views (bez Compose).
- View Binding + Data Binding.
- Architektura: MVVM.
- Reaktywnosc: RxKotlin/RxJava.
- Podzial modulow/warstw: domain, ui, di, data/repo.
- DI: Koin.
- Dane i autentykacja: Firebase Auth (telefon/SMS) + Firestore (docelowo).
- Logowanie: Timber.
- Czysta domena: cala logika biznesowa w domain w use-case'ach z operatorem invoke.
- Repozytoria jako interfejsy; na start mozliwosc dummy/in-memory bez Firebase Auth/Firestore.

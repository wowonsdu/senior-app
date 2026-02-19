# Plan wdrozenia aplikacji Senior App (Kotlin, MVVM, RxKotlin, Koin, Clean Architecture)

## Streszczenie
Plan implementacji Android aplikacji zgodnej z Figmy, bez Jetpack Compose. Architektura: MVVM + Clean Architecture, RxKotlin/RxJava3, DI przez Koin, UI w XML z ViewBinding i DataBinding. Dane i auth w Firebase (Phone Auth + Firestore). Logowanie: telefon+OTP lub kod zaproszenia + OTP. Role logowania: Pacjent i Opiekun (lekarz bez logowania). Relacje many-to-many pacjent <-> wielu opiekunow. Opiekun po pierwszym logowaniu ma onboarding dodania pacjenta, pacjent od razu trafia na ekran pomiarow.

## Zasady kluczowe (ustalenia)
1. Role logowania: tylko Pacjent i Opiekun. Lekarz jest kontaktem do powiadomien (bez logowania).
2. Logowanie:
   - Telefon -> OTP (Firebase Phone Auth).
   - Kod zaproszenia (6 cyfr) + telefon -> OTP (dwa kody).
3. Zaproszenia tworza konta/relacje jako PENDING w Firestore do pierwszego zalogowania.
4. Relacje many-to-many: pacjent moze miec wielu opiekunow; opiekun moze miec wielu pacjentow.
5. Glowny opiekun: pierwszy dodany jest domyslny; tylko glowny opiekun moze zmienic glownego.
6. Kto kogo dodaje:
   - Pacjent dodaje opiekunow i lekarzy.
   - Glowny opiekun moze zarzadzac lekarzami pacjenta.
   - Lekarz niczym nie zarzadza.
7. Powiadomienia:
   - Konfiguracja SMS/email/in-app/push.
   - MVP: push + in-app dla opiekunow; SMS/email jako intencja udostepnienia.
8. Profil pacjenta: jak w Figmie (imie, nazwisko, PESEL, adres, telefon) - opcjonalne.
9. Routing po logowaniu:
   - Pacjent -> zawsze Home (pomiary).
   - Opiekun -> jesli brak pacjentow: PatientSelection (onboarding).
   - Opiekun -> jesli sa pacjenci: CaregiverDashboard.

## Etapy implementacji (wznawialne)

### Etap 1: Szkielet projektu i narzedzia
- Utworzenie modulow: `app`, `domain`, `data` (opcjonalnie `core`).
- Konfiguracja Koin (moduly DI dla data, domain, app).
- Konfiguracja RxJava3/RxKotlin oraz SchedulersProvider.
- Konfiguracja Navigation (auth_graph + main_graph) i MainActivity.
- Ustalenie wzorca ViewBinding + DataBinding.
- Dodanie Firebase SDK (Auth, Firestore, Messaging) i podstawowy config.

**Punkt kontrolny:** aplikacja sie uruchamia, przechodzi na ekran startowy (RoleSelection), DI i nawigacja dzialaja.

### Etap 2: Auth i sesja
- Ekrany: RoleSelection, Login (telefon/kod), OTP.
- Usecase'y: SendPhoneOtp, VerifyPhoneOtp, EnsureUserRecord, ObserveSession, SignOut.
- Implementacja Phone Auth (Firebase).
- Zapisywanie sesji i roli w lokalnym store (DataStore).
- Routing po logowaniu zgodnie z zasadami.

**Punkt kontrolny:** pacjent i opiekun moga zalogowac sie OTP, przekierowanie na wlasciwy start.

### Etap 3: Zaproszenia i aktywacja kont
- Cloud Functions (callable):
  - `createInvite` (tworzy zaproszenie z codeHash).
  - `resolveInvite` (weryfikuje kod + numer, zwraca inviteId).
  - `claimInvite` (po OTP: konsumuje zaproszenie i tworzy relacje).
- Firestore: kolekcje `invites` z polami status/expiry.
- UI: tworzenie zaproszen (pacjent -> opiekun, opiekun -> pacjent).

**Punkt kontrolny:** kod zaproszenia dziala tylko z poprawnym telefonem i OTP, relacja zostaje aktywowana.

### Etap 4: Modele danych i dostepy (Firestore)
- Dokumenty:
  - `users/{uid}` (role, phoneE164, active).
  - `patients/{patientId}` (status, ownerUid, invitedPhoneE164, displayName).
  - Subkolekcje: caregivers, doctors, measurements, alertSettings, alerts, visits, medicines, diseases.
- Security Rules: dostep pacjent/opiekun zgodny z relacja; zarzadzanie lekarzami tylko pacjent lub glowny opiekun.

**Punkt kontrolny:** odczyty/zapisy dzialaja zgodnie z rolami i relacjami.

### Etap 5: Pomiary i historia
- Ekran Home (dodawanie pomiarow) + VoiceInput.
- Usecase'y: AddMeasurement, ObserveMeasurements (filtry), MarkRead, ObserveUnreadCount.
- Ekran History (wykres + lista).
- Normalizacja wartosci (cisnienie ma 2 liczby).

**Punkt kontrolny:** pacjent dodaje pomiary i widzi historie; opiekun widzi historie pacjenta.

### Etap 6: Alerty i powiadomienia
- Ekran AlertSettings z konfiguracja progow, rapid change i przypisania opiekunow.
- Usecase'y: ObserveAlertSettings, UpdateAlertSettings, EvaluateAndCreateAlerts, ObserveAlerts.
- Cloud Function trigger -> FCM do przypisanych opiekunow + zapis in-app alert.
- In-app inbox powiadomien.

**Punkt kontrolny:** przekroczenie progow generuje alerty i push dla opiekunow.

### Etap 7: Kontakty (opiekunowie i lekarze)
- Ekran Contacts (opiekunowie i lekarze).
- Usecase'y: InviteCaregiver, RemoveCaregiver, SetPrimaryCaregiver, AddDoctor/UpdateDoctor/RemoveDoctor.
- Blokady uprawnien dla lekarzy.

**Punkt kontrolny:** pacjent dodaje opiekuna i lekarza; glowny opiekun moze zarzadzac lekarzami.

### Etap 8: Panel opiekuna i wybor pacjenta
- CaregiverDashboard: trendy, unread, przeglad pacjentow.
- PatientSelection: onboarding i wybor aktywnego pacjenta.
- DataStore: zapamietanie aktywnego pacjenta.

**Punkt kontrolny:** opiekun zarzadza pacjentami i przechodzi miedzy kontekstami.

### Etap 9: Wizyty i przypomnienia
- Ekran Visits z CRUD wizyt.
- Usecase'y: Create/Update/Delete/ObserveVisits.
- MVP: przypomnienia jako in-app + opcjonalnie lokalny alarm.

**Punkt kontrolny:** opiekun planuje i widzi wizyty pacjentow.

### Etap 10: SOS i udostepnianie
- Ekran Home: akcja SOS (tel:112 + SMS do kontaktow).
- Usecase'y: BuildEmergencyMessage, GetEmergencyRecipients.

**Punkt kontrolny:** akcja SOS wywolywuje polaczenie i przygotowuje SMS.

### Etap 11: Stabilizacja i testy
- Unit testy w domain (normalizacja, alerty).
- Integracyjne dla repozytoriow (emulatory Firebase).
- UI testy najwazniejszych flow logowania.

**Punkt kontrolny:** scenariusze krytyczne przechodza na emulatorach.

## Dodatkowo (na koniec)
### Firebase Functions (do wdrozenia na koncu)
- `resolveInvite` (kod + telefon -> inviteId)
- `claimInvite` (inviteId -> aktywacja relacji)
- `createInvite` (tworzenie zaproszen przez pacjenta/opiekuna)

## API / interfejsy (kluczowe)
### Domain repository interfaces (przyklad)
- AuthRepository: sendOtp, verifyOtp, observeSession, signOut
- InviteRepository: createInvite, resolveInvite, claimInvite
- PatientRepository: observePatients, observePatient, selectActivePatient, observeActivePatient
- MeasurementRepository: addMeasurement, observeMeasurements, markRead, observeUnreadCount
- AlertRepository: observeSettings, updateSettings, observeAlerts, createAlert
- VisitRepository: createVisit, updateVisit, deleteVisit, observeVisits

## Testy i akceptacja
1. Logowanie pacjenta tel+OTP -> Home.
2. Logowanie opiekuna tel+OTP bez pacjentow -> PatientSelection (onboarding).
3. Kod zaproszenia + tel + OTP -> aktywacja relacji.
4. Opiekun z pacjentami -> CaregiverDashboard.
5. Dodanie pomiaru -> zapis + historia + alerty.
6. Konfiguracja alertow -> push do opiekuna.
7. Dodanie lekarza przez pacjenta i przez glownego opiekuna.

## Zalozenia i ryzyka
- Kod zaproszenia nigdy nie jest przechowywany jawnie (hash w Firestore).
- Brak backendowego SMS/email w MVP (intencje udostepnienia).
- Dla in-app powiadomien i push potrzebne FCM tokeny per urzadzenie.

# Firebase Firestore Emulator

W `DEBUG` aplikacja jest skonfigurowana tak, aby uzywac Firestore Emulatora:
- host: `10.0.2.2`
- port: `8080`

Konfiguracja jest w `app/src/main/java/zdrowy/senior/io/SeniorApp.kt` (wywolanie `FirebaseFirestore.getInstance().useEmulator(...)`).

## Uruchomienie emulatora Firestore
W terminalu na hoście:
```bash
firebase emulators:start --only firestore --project demo-senior-app
```

Albo razem z Auth:
```bash
firebase emulators:start --only auth,firestore --project demo-senior-app
```


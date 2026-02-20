# Firebase Firestore Emulator

W `DEBUG` aplikacja jest skonfigurowana tak, aby uzywac Firestore Emulatora:
- host: `10.0.2.2`
- port: `8082`

Konfiguracja jest w `app/src/main/java/zdrowy/senior/io/SeniorApp.kt` (wywolanie `FirebaseFirestore.getInstance().useEmulator(...)`).

## Uruchomienie emulatora Firestore
Upewnij sie, ze emulator uruchamiasz na JDK 21+ (firebase-tools wymaga Java 21 do emulatorow). Przyklad (PowerShell):
```powershell
$env:JAVA_HOME = "C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.10.7-hotspot"
$env:Path = "$env:JAVA_HOME\\bin;$env:Path"
java -version
```

W terminalu na hoście:
```bash
firebase emulators:start --only firestore --project demo-senior-app
```

Albo razem z Auth:
```bash
firebase emulators:start --only auth,firestore --project demo-senior-app
```

Uwaga: porty emulatorow sa ustawione w `firebase.json` (Auth: `9098`, Firestore: `8082`).

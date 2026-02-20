# Firebase Auth Emulator (Phone / SMS)

App w `DEBUG` jest skonfigurowany tak, aby:
- uzywac Auth Emulator Suite (`10.0.2.2:9098`)
- miec wylaczona weryfikacje aplikacji (`setAppVerificationDisabledForTesting(true)`)

## Uruchomienie emulatora Auth
1. Zainstaluj Firebase CLI (`firebase-tools`).
2. Upewnij sie, ze emulator uruchamiasz na JDK 21+ (firebase-tools wymaga Java 21 do emulatorow). Przyklad (PowerShell):
```powershell
$env:JAVA_HOME = "C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.10.7-hotspot"
$env:Path = "$env:JAVA_HOME\\bin;$env:Path"
java -version
```
2. W terminalu (host) uruchom:
```bash
firebase emulators:start --only auth --project demo-senior-app
```
3. Opcjonalnie otworz Emulator UI (domyslnie `http://localhost:4000`) i dodaj testowy numer telefonu + kod.

Uwaga: port Auth jest ustawiony w `firebase.json` na `9098`.

## Test w aplikacji (Android emulator)
- Numer: np. `+48500100200` (albo wpisz 9 cyfr, app dopisze `+48`)
- Kod SMS: `123456`

## Smoke test ADB
```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke_auth_phone.ps1 -DeviceId emulator-5554
```

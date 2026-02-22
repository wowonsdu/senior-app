# STT Patient Home Plan

## Scope
- UI na ekranie g³ównym pacjenta oraz zapis rozpoznanych pomiarów.

## Modules And Layers
- UI / ViewModel / DI

## Deliverables
- ui/src/main/res/layout/fragment_patient_home.xml
- ui/src/main/kotlin/zdrowy/senior/io/ui/patient/PatientHomeFragment.kt
- ui/src/main/kotlin/zdrowy/senior/io/ui/patient/PatientHomeViewModel.kt
- ui/src/main/kotlin/zdrowy/senior/io/ui/patient/VoiceSaveSummary.kt
- ui/src/main/res/values/strings.xml
- di/src/main/kotlin/zdrowy/senior/io/di/DiModules.kt

## Implementation Checklist
- [ ] Dodaj sekcje glosowa nad kafelkami cukier/insulina.
- [ ] Obsluga RECORD_AUDIO + SpeechRecognizer w PatientHomeFragment.
- [ ] ViewModel: parse -> zapis pomiarow (Schedulers.io + AndroidSchedulers.mainThread).
- [ ] Podsumowanie zapisu (toast) z pominietymi.
- [ ] Aktualizacja DI: ParseVoiceMeasurementsUseCase + nowe VM deps.

## Validation
- [ ] Smoke test: cukier 230 puls 70 cisnienie 250 na 14.
- [ ] Smoke test: cisnienie 120 (pomija).
- [ ] Smoke test: insulina 8 cukier 190.

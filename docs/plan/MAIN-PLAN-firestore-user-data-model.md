# MAIN PLAN — Firestore user/data model

Cel: przygotować kanoniczny model danych Firestore (pod pacjenta i opiekuna) oraz listę etapów migracji repozytoriów InMemory → Firestore.

## Założenia (decision complete)
- Jedno konto Firebase Auth (`uid`) ma jedną stałą rolę: `PATIENT` albo `CAREGIVER`.
- Struktura bazowa: `users/{uid}` + subkolekcje.
- Kontakty pacjenta (opiekun/lekarz) są hybrydą: kontakt może nie mieć konta, a opcjonalnie ma `linkedUid`.
- Alerty: `users/{uid}/settings/notifications` (1 doc, `alerts.{measurementType}.*`).
- Pomiary: `timestampMs` jako `Long` (ms).

## Etap 0 — Kanoniczny schemat i mapowania
- [ ] Opisać kolekcje, dokumenty, pola, ID i query shapes
- [ ] Subplan: docs/plan/firestore-00-canonical-schema.md

## Etap 1 — Dokument `users/{uid}` i bootstrap roli
- [ ] Utworzenie/odczyt profilu konta + walidacja stałej roli
- [ ] Subplan: docs/plan/firestore-01-user-role-bootstrap.md

## Etap 2 — Pacjent: settings (personalData, diseases, medications)
- [ ] Subkolekcje settings + CRUD + mapowanie
- [ ] Subplan: docs/plan/firestore-02-patient-settings.md

## Etap 3 — Pacjent: measurements + historia + wykresy
- [ ] Model pomiarów i zapytania (recent/history/chart)
- [ ] Subplan: docs/plan/firestore-03-patient-measurements.md

## Etap 4 — Kontakty + careLinks + accessCodes (kody dostępu)
- [ ] Kontakty (hybryda) oraz powiązania opiekun↔pacjent i kody
- [ ] Subplan: docs/plan/firestore-04-contacts-carelinks-accesscodes.md

## Etap 5 — Pacjent: alerty
- [ ] Dokumenty alertów per-typ + update’y cząstkowe
- [ ] Subplan: docs/plan/firestore-05-patient-alerts.md

## Etap 6 — Security, indexy i tooling emulatora
- [ ] Reguły (MVP) + indexy + instrukcje uruchomienia i walidacji
- [ ] Subplan: docs/plan/firestore-06-security-indexes-tooling.md


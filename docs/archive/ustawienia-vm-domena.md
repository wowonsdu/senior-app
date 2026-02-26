# Ustawienia: VM/Domena pod nowy uklad Plan

## Scope
- Dostosowanie modelu stanu i logiki prezentacji do nowego ukladu UI
- Brak rozwijania dla danych pacjenta; dolegliwości i leki zawsze widoczne

## Modules And Layers
- UI state (VM, modele UI)
- Domain (ew. mapowanie danych ustawien)

## Deliverables
- Zaktualizowany model stanu ekranu ustawien
- Uproszczona logika sekcji (bez stanu rozwiniecia dla danych pacjenta)
- Spójne dane dla list dolegliwości i lekow

## Implementation Checklist
- [x] Przejrzec aktualny VM/stany dla ustawien i zidentyfikowac zaleznosci od "rozwin/zwij"
- [x] Usunac stan rozwiniecia dla danych pacjenta i uproscic logike sekcji
- [x] Utrzymac listy dolegliwości i lekow jako stale widoczne w stanie UI
- [x] Zaktualizowac mapowanie danych z use case/repo do nowego modelu UI
- [x] Dopasowac adaptery/sekcje list do nowej struktury danych

## Validation
- [x] Szybki smoke-check przeplywu danych w UI (bez zapisu zrzutow do repo)


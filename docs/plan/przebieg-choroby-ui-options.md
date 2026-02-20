# UI: Opcje Przebiegu Choroby — Plan

## Scope
- Aktualizacja listy opcji przebiegu choroby w dialogach dodawania/edycji.

## Modules And Layers
- UI (resources, layouty dialogow)

## Deliverables
- Zaktualizowany `string-array` dla opcji przebiegu choroby.

## Implementation Checklist
- [ ] Zmienic `patient_add_disease_course_options` na 4 wartosci: Lekki, Sredni, Ciezki, Bardzo ciezki.
- [ ] Sprawdzic, ze dialogi `Dodaj chorobe` i `Edytuj chorobe` korzystaja z tego samego array.

## Validation
- [ ] Otworzyc oba dialogi i potwierdzic 4 opcje w dropdownie.

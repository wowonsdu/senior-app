# Plan — Leki: przeniesienie dodaj/edytuj do fragmentow

## Etap 1 — Nawigacja + szkielety fragmentow
- [ ] Nowe destinations w `nav_graph.xml` (fragmenty zamiast dialogow)
- [ ] Nowe layouty fragmentow ze scrollem
- [ ] Subplan: docs/plan/lek-fragment-01-nawigacja-szkielety.md

## Etap 2 — Dodaj lek: zapis
- [ ] Add screen: walidacja + zapis przez ViewModel (Rx na `Schedulers.io`)
- [ ] Powrot do ustawien po sukcesie (bez FragmentResult)
- [ ] Subplan: docs/plan/lek-fragment-02-dodaj-lek.md

## Etap 3 — Edytuj/usun lek: prefill + delete UX
- [ ] Edit screen: prefill reaktwny (observe) + update
- [ ] Usun: potwierdzenie + remove + powrot
- [ ] Subplan: docs/plan/lek-fragment-03-edytuj-usun-lek.md

## Etap 4 — Cleanup + smoke check
- [ ] Usuniecie starych dialogow i ich layoutow
- [ ] Smoke check: dodaj/edytuj/usun; 0/1/10 + odswiezanie listy przez observe
- [ ] Subplan: docs/plan/lek-fragment-04-cleanup-qa.md


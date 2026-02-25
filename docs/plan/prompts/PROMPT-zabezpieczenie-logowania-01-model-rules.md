Zadanie:
Zaimplementuj subplan: docs/plan/zabezpieczenie-logowania-01-model-rules.md

Zakres:
- Tylko firestore schema/rules/indexes i dokumentacja.
- Nie ruszaj UI ani domeny.

DoD:
- accessCodes/{code} nie da sie odczytac bez dopasowanego request.auth.token.phone_number
- Proby "get random code" z innego numeru koncza sie PERMISSION_DENIED


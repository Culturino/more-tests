# chi

Agenda **#1** — tiny categorical table for `chi_sq` with incomplete rows.

Source TTL: [`src/test/resources/custom-data/chi.ttl`](../src/test/resources/custom-data/chi.ttl)

| id | type | label | gender | order |
|----|------|-------|--------|-------|
| PersonA | Person | A | Male | Chocolate |
| PersonB | Person | B | Male | Vanilla |
| PersonC | Person | C | Female | Chocolate |
| PersonD | Person | D | Female | Vanilla |
| PersonE | Person | E | Male | — |
| PersonF | Person | F | — | Strawberry |
| PersonG | Person | G | — | — |

`—` = property absent in the graph.

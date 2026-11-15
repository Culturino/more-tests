# pairs

Agenda **#1** — incomplete math / english score pairs.

Source TTL: [`src/test/resources/custom-data/pairs.ttl`](../src/test/resources/custom-data/pairs.ttl)

| id | type | math | english | note |
|----|------|------|---------|------|
| alice | Student | 0 | — | left-only (`[0, …]`) |
| bob | Student | 1 | — | left-only (`[…, 1]`) |
| noah | Student | 5 | — | left-only |
| cara | Student | 2 | 4 | complete (on line `english = 2×math`) |
| dan | Student | 3 | 6 | complete |
| ella | Student | 4 | 8 | complete |
| finn | Student | 10 | 20 | complete |
| eve | Student | — | 9 | right-only |
| gina | Student | — | 1 | right-only |
| hank | Student | — | — | both missing |
| iris | Student | 7 | 0 | complete (off-line) |
| jake | Student | 8 | 3 | complete (off-line) |

`—` = property absent in the graph.

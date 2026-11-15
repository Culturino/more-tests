# edge_dirty

Agenda **#4** — dirty / weird values.

Source TTL: [`src/test/resources/custom-data/edge_dirty.ttl`](../src/test/resources/custom-data/edge_dirty.ttl)

| id | case | val | other | flag | note |
|----|------|-----|-------|------|------|
| n1 | clean | 10 | — | true | clean numerics |
| n2 | clean | 20 | — | true | |
| n3 | clean | 30 | — | false | |
| s1 | stringNum | `"10"` (string) | — | — | string pretending to be a number |
| s2 | stringNum | 20 | — | — | |
| m1 | mixed | 1 | 2 | — | mixed clean + dirty |
| m2 | mixed | `"oops"` (string) | 4 | — | poisons numeric aggregates |
| m3 | mixed | 3 | 6 | — | |
| t1 | allTrue | — | — | true | |
| t2 | allTrue | — | — | true | |
| f1 | allFalse | — | — | false | |
| f2 | allFalse | — | — | false | |
| d1 | dup | 1, 1, 2 | — | — | multi-valued property (3 observations) |
| c1 | emptyCond | 10 | — | false | condition never true |
| c2 | emptyCond | 20 | — | false | |

`—` = property absent in the graph.

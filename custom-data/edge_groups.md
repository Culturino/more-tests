# edge_groups

Agenda **#3** — `GROUP BY` slices (healthy / sparse / identical / incomplete).

Source TTL: [`src/test/resources/custom-data/edge_groups.ttl`](../src/test/resources/custom-data/edge_groups.ttl)

| id | group | math | english | note |
|----|-------|------|---------|------|
| ga1 | A | 1 | 2 | perfect line `english = 2×math` |
| ga2 | A | 2 | 4 | |
| ga3 | A | 3 | 6 | |
| ga4 | A | 4 | 8 | corr → 1.0 |
| gb1 | B | 9 | 1 | single pair → pearson NaN |
| gc1 | C | 5 | 5 | identical pairs → NaN |
| gc2 | C | 5 | 5 | |
| gd1 | D | 7 | — | incompletes only (absent under REQUIRED join) |
| gd2 | D | — | 3 | |

`—` = property absent in the graph.

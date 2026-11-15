# tiny

Agenda **#2** — degenerate / tiny groups. Slice with `ex:case`.

Source TTL: [`src/test/resources/custom-data/tiny.ttl`](../src/test/resources/custom-data/tiny.ttl)

| id | case | x | y | note |
|----|------|---|---|------|
| empty1 | empty | 1 | — | no complete pairs in this case |
| empty2 | empty | — | 2 | |
| empty3 | empty | — | — | |
| single1 | single | 3 | 4 | exactly one complete pair |
| id1 | identical | 5 | 5 | no variance |
| id2 | identical | 5 | 5 | |
| id3 | identical | 5 | 5 | |
| cx1 | constantX | 2 | 1 | x fixed → pearson NaN |
| cx2 | constantX | 2 | 3 | |
| cx3 | constantX | 2 | 7 | |
| two1 | two | 1 | 2 | smallest healthy line (`y = 2x`) |
| two2 | two | 2 | 4 | corr ≈ 1 |

`—` = property absent in the graph.

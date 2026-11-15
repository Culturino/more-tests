# Edge-case testing report

This project adds statistics functions to SPARQL (query language for graph data): correlation, covariance, percentile, probability, chi-squared, and related helpers.

Earlier tests mostly copied the same happy-path pattern. We added **edge-case** coverage in four agenda points, with small handmade datasets (TTL under [`src/test/resources/custom-data/`](src/test/resources/custom-data/), table views under [`custom-data/`](custom-data/)) plus a few tests on real fixtures ([`cars.ttl`](src/test/resources/cars.ttl), [`cities.ttl`](src/test/resources/cities.ttl)).

All of this lives mainly in:

- Table views: [`custom-data/`](custom-data/)
- TTL sources: [`src/test/resources/custom-data/`](src/test/resources/custom-data/)
- Tests: [`EdgeCaseTest.java`](src/test/java/com/kari3600/me/statistics_rdf4j/EdgeCaseTest.java)
- Helpers: [`TestUtil.java`](src/test/java/com/kari3600/me/statistics_rdf4j/TestUtil.java) (`queryAll`, unbound checks)

Full suite: **green** locally (`mvn test`).

See also the project overview in [`README.md`](README.md).

---

## What “null” means here

SPARQL/RDF has no JSON-style `null`. “Missing” shows up as:

1. **Property absent + required pattern** → that row never joins (dropped).
2. **`OPTIONAL { ... }`** → row still exists, but the variable is **unbound** (blank).
3. **Wrong type** (e.g. string `"10"` instead of number `10`) → type error inside the aggregate.

---

## Custom datasets

| File | Agenda | Role |
|------|--------|------|
| [`edge_pairs`](custom-data/edge_pairs.md) | #1 | Students with `math` / `english`; some missing one or both |
| [`edge_chi`](custom-data/edge_chi.md) | #1 | Tiny gender×order table with incomplete people |
| [`edge_tiny`](custom-data/edge_tiny.md) | #2 | Degenerate sizes via `ex:case` |
| [`edge_groups`](custom-data/edge_groups.md) | #3 | Groups for `GROUP BY` / FILTER / multi-agg |
| [`edge_dirty`](custom-data/edge_dirty.md) | #4 | Dirty / weird typed values |

---

## Agenda #1 — Incomplete / null pairs

### Goal

What if we correlate something like `[0, 1]` with missing partners — one side has numbers, the other is blank?

### What the code does (simple)

- **OPTIONAL tests:** allow missing values in, call `stat:pearson` / `covariance` / `percentile` / `p` / etc.
- **Assert:** usually one result row with the answer variable **unbound** (empty).
- **REQUIRED tests:** both properties required → incompletes disappear; known numbers (e.g. perfect line → pearson `1.0`).
- **`EDGE_*`:** same idea on [`cars.ttl`](src/test/resources/cars.ttl) / [`cities.ttl`](src/test/resources/cities.ttl).

### What we learned

| Situation | Typical result |
|-----------|----------------|
| OPTIONAL missing into pearson / cov / percentile / median / probability | **Unbound** (empty answer) |
| One missing row in a mixed group | **Poisons the whole aggregate** |
| REQUIRED both sides | Incompletes dropped; math works on the rest |
| `chi_sq` + OPTIONAL missing | **Still returns a number** (treats blanks like a category) |

Incomplete pair case (alice+bob, OPTIONAL english): **empty correlation**, not a crash.

---

## Agenda #2 — Tiny / empty groups

### Goal

Almost no data — too few points or no spread for correlation.

### Dataset: [`edge_tiny`](custom-data/edge_tiny.md)

| Case | Meaning |
|------|---------|
| `empty` | No complete `(x,y)` pairs |
| `single` | Exactly one pair |
| `identical` | Several copies of the same pair |
| `constantX` | `x` fixed, `y` changes |
| `two` | Smallest healthy line (`y = 2x`) → corr ≈ 1 |

### What we learned

| Case / function | Result |
|-----------------|--------|
| pearson on empty / single / identical / constantX | **NaN** |
| covariance on those | **0.0** |
| percentile with zero inputs | **NullPointerException** |
| probability with zero rows | **NaN** (0/0) |
| `two` (two good points) | pearson **1**, covariance **1** |

---

## Agenda #3 — Richer queries

### Dataset: [`edge_groups`](custom-data/edge_groups.md)

| Group | Content |
|-------|---------|
| **A** | 4 points on a perfect line → corr 1 |
| **B** | 1 pair → degenerate |
| **C** | Identical pairs → no variance |
| **D** | Incomplete only → never appears under REQUIRED join |

### What we learned

- **`GROUP BY`:** A → `1.0` / n=4; B & C → `NaN`; D absent.
- Empty **`FILTER`** → NaN (bound), not unbound.
- Multi-aggregate **`SELECT`** (pearson + covariance + count) works on a healthy group.

---

## Agenda #4 — Dirty / weird values

### Dataset: [`edge_dirty`](custom-data/edge_dirty.md)

| Test idea | Result |
|-----------|--------|
| String `"10"` mixed into percentile | **Unbound** |
| `"oops"` mixed into pearson | **Unbound** |
| `stat:p(?flag)` with raw RDF boolean | **Unbound** (use `?flag = true`) |
| `stat:p(?flag = true)` all true / all false | **1.0** / **0.0** |
| Conditional prob with condition never true | **NaN** |
| Multi-valued `1,1,2` percentile | **≈2.0** |
| Percentile rank `1.5` (outside 0..1) | **Throws** |
| Clean baseline `p(val > 15)` | **2/3** |

---

## Code map

```
custom-data/*.md                      # table views (docs)
src/test/resources/custom-data/*.ttl  # TTL sources (tests)

src/test/java/.../EdgeCaseTest.java
  optional* / required*     → #1 handmade
  EDGE_*                    → #1 on natural + chi
  tiny*                     → #2
  richer*                   → #3
  dirty*                    → #4

src/test/java/.../TestUtil.java
  query1Group1Var  → one number
  queryAll         → multi-row / unbound checks
```

`assertUnboundAggregate` means: run this query, expect exactly one row, and the named variable has **no value**.

---

## Big takeaways

1. **Missing values via `OPTIONAL`:** most stats → **unbound**; one bad row can poison the group; **chi_sq** still returns a number.
2. **Tiny data:** pearson → **NaN**; covariance often → **0**; empty percentile can **NPE**.
3. **Richer SPARQL works:** `GROUP BY`, empty `FILTER`, multi-aggregate selects are covered.
4. **Dirty types:** unbound or throw; boolean flags need `= true`; percentile arg must be in `[0,1]`.

Happy-path clones on DBpedia-style data remain; these edge tests add the interesting coverage.

---

## Out of scope (next)

- EDA writeup per existing DBpedia-style dataset.
- Changing library behavior (tests document current behavior; they don’t fix NaN/NPE/unbound policies).

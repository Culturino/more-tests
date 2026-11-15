# testing2 — edge-case findings

Status of the SPARQL statistics edge suite in this repo (RDF4J aggregates: pearson, covariance, percentile / approx_median, probability, chi-squared).

## Current layout

| Piece | Where |
|-------|--------|
| Findings (this doc) | [`testing2.md`](testing2.md) |
| Project overview | [`README.md`](README.md) |
| Per-dataset EDA | [`eda/`](eda/) |
| Line-by-line walkthrough | [`post analysis/`](post%20analysis/) |
| Fixture table views | [`custom-data/`](custom-data/) |
| Fixture TTL (what tests load) | [`src/test/resources/custom-data/`](src/test/resources/custom-data/) |
| Edge tests (by function) | `*EdgeTest.java` under [`src/test/java/.../statistics_rdf4j/`](src/test/java/com/kari3600/me/statistics_rdf4j/) |
| Shared helpers | [`TestUtil.java`](src/test/java/com/kari3600/me/statistics_rdf4j/TestUtil.java) (`queryAll`, `assertUnboundAggregate`) |

**How the suite is organized now**

- Edge coverage is split **by SPARQL function**, not one monolith: `PearsonEdgeTest`, `CovarianceEdgeTest`, `PercentileEdgeTest`, `ProbabilityEdgeTest`, `ChiSquaredEdgeTest`.
- Handmade fixtures are named **`pairs` / `chi` / `tiny` / `groups` / `dirty`** (no `EDGE_` / `edge_` prefixes).
- Method names drop old `result` / agenda suffixes; natural controls (cars/cities) stay first where relevant.
- Full suite: **green** locally (`mvn test`, `mvn test "-Dtest=*EdgeTest"`).

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
| [`pairs`](custom-data/pairs.md) | #1 | Students with `math` / `english`; some missing one or both |
| [`chi`](custom-data/chi.md) | #1 | Tiny gender×order table with incomplete people |
| [`tiny`](custom-data/tiny.md) | #2 | Degenerate sizes via `ex:case` |
| [`groups`](custom-data/groups.md) | #3 | Groups for `GROUP BY` / FILTER / multi-agg |
| [`dirty`](custom-data/dirty.md) | #4 | Dirty / weird typed values |

EDA for these (and the natural DBpedia-style fixtures) lives under [`eda/`](eda/).

---

## Agenda #1 — Incomplete / null pairs

### Goal

What if we correlate something like `[0, 1]` with missing partners — one side has numbers, the other is blank?

### What the code does

- **OPTIONAL tests:** allow missing values in, call `stat:pearson` / `covariance` / `percentile` / `p` / etc.
- **Assert:** usually one result row with the answer variable **unbound** (empty) via `assertUnboundAggregate`.
- **REQUIRED tests:** both properties required → incompletes disappear; known numbers (e.g. perfect line → pearson `1.0`).
- **Natural fixtures:** same idea on [`cars.ttl`](src/test/resources/cars.ttl) / [`cities.ttl`](src/test/resources/cities.ttl).

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

### Dataset: [`tiny`](custom-data/tiny.md)

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

### Dataset: [`groups`](custom-data/groups.md)

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

Covered mainly in `PearsonEdgeTest` (`richerGroupBy_pearson`, `richerFilterEmpties_pearson`, `richerMultiAggregate_pearson`).

---

## Agenda #4 — Dirty / weird values

### Dataset: [`dirty`](custom-data/dirty.md)

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

```text
custom-data/*.md                      # table views (docs)
src/test/resources/custom-data/*.ttl  # TTL sources (tests)

*EdgeTest.java                        # ~52 edge tests, by function
  PearsonEdgeTest      (~16)  pearson (#1–#4 slices)
  CovarianceEdgeTest   (~12)  covariance
  PercentileEdgeTest   (~11)  percentile / approx_median
  ProbabilityEdgeTest  (~10)  p / conditional p
  ChiSquaredEdgeTest   (~3)   chi_sq

TestUtil.java
  query1Group1Var        → one number
  queryAll               → multi-row / unbound checks
  assertUnboundAggregate → single unbound aggregate row
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

## Done vs still open

| Done | Notes |
|------|--------|
| Agendas #1–#4 encoded as tests | Green under `*EdgeTest` |
| Suite split by function | Replaces former single `EdgeCaseTest` |
| Fixture rename + table views | `pairs` / `chi` / `tiny` / `groups` / `dirty` |
| Per-dataset EDA | [`eda/`](eda/) |
| Walkthrough docs | [`post analysis/`](post%20analysis/) (00–06) |

| Still open | Notes |
|------------|--------|
| Changing library behavior | Tests **document** current NaN / NPE / unbound policies; they do not fix them |
| Happy-path / `src/main` deep dive | Optional; not part of this edge report |

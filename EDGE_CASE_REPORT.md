# Edge-case testing report

This project adds statistics functions to SPARQL (query language for graph data): correlation, covariance, percentile, probability, chi-squared, and related helpers.

Kari’s feedback was that earlier tests mostly copied the same happy-path pattern. We added **edge-case** coverage in four agenda points, with small handmade datasets plus a few tests on real fixtures (`cars.ttl`, `cities.ttl`).

All of this lives mainly in:

- Datasets: `src/test/resources/edge_*.ttl`
- Tests: `src/test/java/.../EdgeCaseTest.java`
- Helpers: `TestUtil.java` (`queryAll`, unbound checks)

Full suite: **green** locally (`mvn test`).

---

## What “null” means here

SPARQL/RDF has no JSON-style `null`. “Missing” shows up as:

1. **Property absent + required pattern** → that row never joins (dropped).
2. **`OPTIONAL { ... }`** → row still exists, but the variable is **unbound** (blank).
3. **Wrong type** (e.g. string `"10"` instead of number `10`) → type error inside the aggregate.

---

## Agenda #1 — Incomplete / null pairs

### Goal

Answer Kari’s example: what if we correlate something like `[0, 1]` with `[null, null]` — one side has numbers, the other is missing?

### Mini datasets

| File | Role |
|------|------|
| `edge_pairs.ttl` | Students with `math` / `english`; some missing one or both |
| `edge_chi.ttl` | Tiny gender×order table with some incomplete people |

Also reused **`cars.ttl`** and **`cities.ttl`** (already incomplete in the wild) via `EDGE_*` tests.

### What the code does (simple)

- **OPTIONAL tests:** load students/cars/cities, allow missing values in, call `stat:pearson` / `covariance` / `percentile` / `p` / etc.
- **Assert:** usually one result row with the answer variable **unbound** (empty) — not a crash, not a useful number.
- **REQUIRED tests:** both properties required → incompletes disappear; we check known numbers (e.g. perfect line → pearson `1.0`).
- **`EDGE_*`:** same idea on real datasets so Kari can see names like `EDGE_carsOptionalMissingWeight_pearson`.

### What we learned

| Situation | Typical result |
|-----------|----------------|
| OPTIONAL missing into pearson / cov / percentile / median / probability | **Unbound** (empty answer) |
| One missing row in a mixed group | **Poisons the whole aggregate** (completes don’t save it) |
| REQUIRED both sides | Incompletes dropped; math works on the rest |
| `chi_sq` + OPTIONAL missing | **Still returns a number** (treats blanks like a category) — different from the others |

Kari’s `[0,1]` vs `[null,null]` (alice+bob, OPTIONAL english): **empty correlation**, not a crash.

---

## Agenda #2 — Tiny / empty groups

### Goal

Not “some values missing,” but “almost no data” — too few points or no spread for correlation to make sense.

### Mini dataset: `edge_tiny.ttl`

Slices via `ex:case`:

| Case | Meaning |
|------|---------|
| `empty` | No complete `(x,y)` pairs |
| `single` | Exactly one pair |
| `identical` | Several copies of the same pair (no variation) |
| `constantX` | `x` fixed, `y` changes (std of x = 0) |
| `two` | Smallest healthy line (`y = 2x`) → corr ≈ 1 |

### What the code does (simple)

For each slice, run pearson / covariance / percentile / probability and lock whatever the library actually returns.

### What we learned

| Case / function | Result |
|-----------------|--------|
| pearson on empty / single / identical / constantX | **NaN** |
| covariance on those | **0.0** |
| percentile with zero inputs | **NullPointerException** (bug-ish in approx collector) |
| probability with zero rows | **NaN** (0/0) |
| `two` (two good points) | pearson **1**, covariance **1** |

So: tiny data doesn’t always “fail loudly”; sometimes you get **NaN**, sometimes **0**, sometimes a **crash**.

---

## Agenda #3 — Richer queries

### Goal

Stop only testing “one `SELECT`, one aggregate, one number.” Use queries closer to real use.

### Mini dataset: `edge_groups.ttl`

| Group | Content |
|-------|---------|
| **A** | 4 points on a perfect line → corr 1 |
| **B** | 1 pair → degenerate |
| **C** | Identical pairs → no variance |
| **D** | Incomplete only → never appears under REQUIRED join |

### What the code does (simple)

1. **`GROUP BY ?g`** — pearson + `COUNT(*)` per group.  
   Expect: A → `1.0` / n=4; B & C → `NaN`; D absent.
2. **`FILTER` that kills all rows** (`math > 1000` on group A) → pearson **NaN**.
3. **Multi-aggregate `SELECT`** — pearson + covariance + count in one query on group A → `1.0`, `≈3.333`, `n=4`.

### What we learned

- Grouping works; sparse groups show the same degenerate behavior as agenda #2.
- Empty filter → NaN (bound), not unbound.
- Several aggregates in one query work together on a healthy group.

---

## Agenda #4 — Dirty / weird values

### Goal

Junk input: strings as numbers, mixed types, boolean quirks, duplicate values, bad percentile argument.

### Mini dataset: `edge_dirty.ttl`

Cases: `clean`, `stringNum`, `mixed`, `allTrue`, `allFalse`, `dup`, `emptyCond`.

### What the code does (simple)

Run percentile / pearson / probability on each dirty slice and assert:

| Test idea | Result |
|-----------|--------|
| String `"10"` mixed into percentile | **Unbound** |
| `"oops"` mixed into pearson | **Unbound** |
| `stat:p(?flag)` with raw RDF boolean | **Unbound** (library wants a SPARQL boolean expression) |
| `stat:p(?flag = true)` all true / all false | **1.0** / **0.0** |
| Conditional prob with condition never true | **NaN** |
| Multi-valued `1,1,2` percentile | **≈2.0** |
| Percentile rank `1.5` (outside 0..1) | **Throws** |
| Clean baseline `p(val > 15)` | **2/3** |

### What we learned

- Bad types don’t crash the query; they usually wipe the answer (unbound).
- Prefer boolean **expressions** (`?flag = true`) for `stat:p`, not raw flags.
- Out-of-range percentile is a hard error from the sketch library.

---

## Code map (where to look)

```
src/test/resources/
  edge_pairs.ttl    #1 nulls (students)
  edge_chi.ttl      #1 chi_sq gaps
  edge_tiny.ttl     #2 tiny / empty
  edge_groups.ttl   #3 GROUP BY
  edge_dirty.ttl    #4 dirty values
  cars.ttl, cities.ttl   # reused by EDGE_* tests

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

### Helper pattern (simple)

`assertUnboundAggregate` means: “run this query, expect exactly one row, and the named variable has **no value**.”

That’s how we document “empty answer” without pretending we got a correlation.

---

## Big takeaways for Kari

1. **Missing values via `OPTIONAL`:** most stats → **unbound**; one bad row can poison the group; **chi_sq** still returns a number.
2. **Tiny data:** pearson → **NaN**; covariance often → **0**; empty percentile can **NPE**.
3. **Richer SPARQL works:** `GROUP BY`, empty `FILTER`, multi-aggregate selects are covered.
4. **Dirty types:** unbound or throw; boolean flags need `= true`; percentile arg must be in `[0,1]`.

Happy-path clones on DBpedia data remain; these edge tests are the “interesting” coverage he asked for.

---

## Not in this report (next)

- **EDA writeup per existing DBpedia dataset** (separate Kari request).
- Changing library behavior (tests document current behavior; they don’t fix NaN/NPE/unbound policies).

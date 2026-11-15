# 2 — `PearsonEdgeTest.java` (full walkthrough)

Source: [`src/test/java/com/kari3600/me/statistics_rdf4j/PearsonEdgeTest.java`](../src/test/java/com/kari3600/me/statistics_rdf4j/PearsonEdgeTest.java)

This class is **only** weird/edge tests for `stat:pearson` (correlation).  
`@Test` on a method = JUnit runs that method when you `mvn test`.

Fixture tables: [`custom-data/pairs.md`](../custom-data/pairs.md), [`tiny.md`](../custom-data/tiny.md), [`groups.md`](../custom-data/groups.md), [`dirty.md`](../custom-data/dirty.md).

> Test method names match the Java `@Test` methods (result words stripped from both).

---

## Class header & shared constants

```java
private static final String PREFIX = """
        PREFIX ex: <http://example.org/edge#>
        """;

private static final String DBPEDIA = """
        PREFIX dbo: <http://dbpedia.org/ontology/>
        PREFIX dbp: <http://dbpedia.org/property/>
        """;
```

- Shortcuts so SPARQL can say `ex:alice` or `dbo:City` instead of giant URLs.
- `PREFIX` → handmade edge data.
- `DBPEDIA` → real-ish cars/cities fixtures.

### Shared helper — `assertTinyPearson(caseName)`

Used by the tiny-group tests later.

| Piece | Meaning |
|-------|---------|
| `caseName` | e.g. `"empty"`, `"single"` — a label in the tiny dataset |
| `SELECT (stat:pearson(?x,?y) AS ?corr)` | Compute correlation; name the answer `?corr` |
| `?s ex:case "%s"` | Only take points tagged with that case |
| `.formatted(caseName)` | Stick the case name into the `%s` |
| `.doubleValue()` | Turn the RDF number into a Java `double` |
| `Double.isNaN(d)` | “Not a Number” — correlation undefined |
| `assertTrue(...)` | Fail if we somehow got a normal number |

**Contrast:**

| Situation | Typical result |
|-----------|----------------|
| `OPTIONAL` + blanks | **unbound** (empty cell) |
| REQUIRED but degenerate / empty | **NaN** |

---

## Block — `pairs` (optional vs required)

**Dataset:** [`pairs`](../custom-data/pairs.md)

| student | math | english |
|---------|------|---------|
| alice, bob, noah | yes | **missing** |
| cara, dan, ella, finn | yes | yes (perfect line: english = 2×math) |
| iris, jake | yes | yes (messy, not on that line) |
| eve, gina | **missing** | yes |
| hank | **missing** | **missing** |

### Healthy controls (first)

#### `requiredPerfectLine_pearson`

```java
assertEquals(1.0, query1Group1Var(...).doubleValue(), 1e-9);
// VALUES cara dan ella finn
// REQUIRED math + english (no OPTIONAL)
```

| Piece | Meaning |
|-------|---------|
| `assertEquals(1.0, actual, 1e-9)` | Expect ≈ 1.0; allow tiny float error |
| `VALUES { cara dan ella finn }` | Only these four |
| required both scores | Missing english ⇒ row dropped |

Points `(2,4) (3,6) (4,8) (10,20)` → english = 2×math.

> **Result:** `1.0`

#### `requiredAllComplete_pearson`

- Every student, still **required** math + english.
- alice/bob/eve/hank never enter.
- Remaining: perfect-line four **plus** iris `(7,0)` and jake `(8,3)`.
- Iris/jake wreck the perfect line. Tolerance `1e-6`.

Lesson: under REQUIRED, incompletes are silently dropped; the number is only over people with **both** scores.

> **Result:** `≈ 0.45191786568701003`

---

### Edge cases

#### `optionalAllYMissing_pearson`

```java
assertUnboundAggregate("custom-data/pairs.ttl", PREFIX + """
        SELECT (stat:pearson(?math, ?english) AS ?corr)
        WHERE {
            VALUES ?student { ex:alice ex:bob }
            ?student ex:math ?math .
            OPTIONAL { ?student ex:english ?english }
        }
        """, "corr");
```

1. Load `pairs.ttl`.
2. Only **alice** and **bob** (`VALUES`).
3. They have math scores.
4. `OPTIONAL` english = keep the row even if english is blank.
5. No english → every pair is `(math, blank)`.

> **Result:** unbound (`?corr` blank)

#### `optionalMixed_pearson`

- Every student with math; english via `OPTIONAL`.
- Some rows fine `(2,4)`, some broken `(0, blank)`.
- One blank ruins the whole aggregate, even though good pairs existed.

> **Result:** unbound

#### `requiredIncompleteOnly_pearson`

**Goal:** What if nobody has both scores?

Only alice & bob, but **required** english (no `OPTIONAL`) → both thrown out → **zero pairs**.

| Version | What enters pearson |
|---------|---------------------|
| OPTIONAL (`optionalAllYMissing`) | blanks go **in** |
| REQUIRED (this test) | list is **empty** |

```java
List<BindingSet> rows = queryAll(...);
assertEquals(1, rows.size());
Value v = rows.get(0).getValue("corr");
// blank OK, or NaN/Inf OK — not a normal float
```

Analogy: empty room — don’t answer “average height = 170.”

> **Result:** unbound **or** NaN/Inf (not a normal correlation)

---

## Block — cars & cities

**Datasets:** [`cars.ttl`](../src/test/resources/cars.ttl), [`cities.ttl`](../src/test/resources/cities.ttl)

Same OPTIONAL idea on scraped data. Happy-path contrast: if the missing property is **required**, incompletes drop and pearson runs on completes only (`PearsonTest`).

### `carsOptionalMissingWeight_pearson`

| Piece | Plain English |
|-------|----------------|
| `"cars.ttl"` | ~163 cars |
| required `length` | every car has length |
| `OPTIONAL` weight | weight if present, else blank |

From EDA: ~60 have weight, ~103 don’t → many `(length, blank)`.

> **Result:** unbound

### `citiesOptionalMissingElevation_pearson`

| cars | cities |
|------|--------|
| length (always) | population (always) |
| weight (often missing) | elevation (often missing) |
| ~37% complete | ~82% complete |

> **Result:** unbound

---

## Block — tiny groups (`tiny`)

**Dataset:** [`tiny`](../custom-data/tiny.md)

| case | Points | Role |
|------|--------|------|
| `two` | `(1,2) (2,4)` | **healthy control** — smallest valid line |
| `empty` | no complete pairs | degenerate |
| `single` | `(3,4)` | degenerate |
| `identical` | three × `(5,5)` | degenerate |
| `constantX` | `(2,1) (2,3) (2,7)` | degenerate |

### Healthy control (first)

#### `tinyTwo_pearson`

Only `(1,2)` and `(2,4)` → `y = 2x`. Proves tiny isn’t always broken.

> **Result:** `1.0`

### Edge cases

#### `tinyEmpty_pearson`

Empty classroom — no complete pairs.

> **Result:** NaN

#### `tinySingle_pearson`

One point isn’t a trend.

> **Result:** NaN

#### `tinyIdentical_pearson`

Everyone the same — no relationship to measure.

> **Result:** NaN

#### `tinyConstantX_pearson`

X never changes — correlation with X is meaningless.

> **Result:** NaN

---

## Block — richer queries (`groups`)

**Dataset:** [`groups`](../custom-data/groups.md)

| Group | Pairs | Role |
|-------|-------|------|
| A | 4 on perfect line | **healthy** |
| B | 1 pair | degenerate |
| C | identical | degenerate |
| D | incompletes only | absent under REQUIRED |

### Healthy control (first)

#### `richerMultiAggregate_pearson`

One query, three stats on **group A** only:

| Variable | Meaning |
|----------|---------|
| `?corr` | pearson |
| `?cov` | covariance |
| `?n` | count |

Proves pearson still works when it shares a `SELECT` with other aggregates.

> **Result:** `corr = 1.0`, `cov ≈ 3.333`, `n = 4`

### Edge cases

#### `richerGroupBy_pearson`

```sparql
SELECT ?g (stat:pearson(?math, ?english) AS ?corr) (COUNT(*) AS ?n)
GROUP BY ?g
ORDER BY ?g
```

Don’t compute one correlation for everyone — one **per group**. Group D disappears (no complete pairs).

> **Result:** 3 rows — A: `corr=1.0, n=4` · B: `corr=NaN, n=1` · C: `corr=NaN, n=2` · **D absent**

#### `richerFilterEmpties_pearson`

Start from healthy group A, then `FILTER(?math > 1000)` → nobody left.

> **Result:** NaN

---

## Block — dirty values (`dirty`)

**Dataset:** [`dirty`](../custom-data/dirty.md)

### `dirtyMixedNonNumeric_pearson`

`mixed` case:

| val | other |
|-----|-------|
| 1 | 2 |
| `"oops"` (text) | 4 |
| 3 | 6 |

Pearson needs numbers. One string → type error, even though two pairs were fine.

> **Result:** unbound

---

## Complete map

| Theme | What we learned |
|-------|------------------|
| OPTIONAL missing | unbound |
| REQUIRED complete | real numbers; incompletes dropped |
| cars/cities | same on real data |
| tiny / FILTER empty | NaN |
| GROUP BY | per-group; empty groups absent |
| dirty string | unbound |

**16 tests** in this file.

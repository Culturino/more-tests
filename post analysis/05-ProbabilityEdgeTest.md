# 5 — `ProbabilityEdgeTest.java` (full walkthrough)

Source: [`src/test/java/com/kari3600/me/statistics_rdf4j/ProbabilityEdgeTest.java`](../src/test/java/com/kari3600/me/statistics_rdf4j/ProbabilityEdgeTest.java)

> Test method names match the Java `@Test` methods (result words stripped from both).

## What `stat:p` is (kid version)

- **`stat:p(?somethingIsTrue)`** — “What fraction of rows is this true for?”  
  Example: `stat:p(?weight > 1000)`.
- **`stat:p(?event, ?condition)`** — **conditional**: “Among rows where the condition is true, how often is the event true?”  
  Example: `stat:p(?weight > 1000, ?length > 100)`.

Answers are usually between 0 and 1 (or NaN / unbound when things break).

---

## Class setup

Same `PREFIX` / `DBPEDIA` as the other edge classes.

---

## Block — OPTIONAL blanks

**Datasets:** [`cars.ttl`](../src/test/resources/cars.ttl), [`cities.ttl`](../src/test/resources/cities.ttl), [`pairs`](../custom-data/pairs.md)

Blank variables from `OPTIONAL` → `?prob` unbound.

### `carsOptionalMissingWeight_probability`

`p(?weight > 1000)` with optional weight.

> **Result:** unbound

### `carsOptionalMissingWeight_conditionalProbability`

`p(?weight > 1000, ?length > 100)` with optional weight **and** length. Both args can be unbound on some rows.

> **Result:** unbound

### `citiesOptionalMissingElevation_probability`

`p(?elevation > 1000)` with optional elevation.

> **Result:** unbound

### `pairsOptionalEnglish_probability`

`p(?english > 5)` with optional english.

> **Result:** unbound

---

## Block — empty input (`tiny`)

**Dataset:** [`tiny`](../custom-data/tiny.md)

### `tinyEmpty_probability`

```sparql
VALUES ?s { ex:empty2 ex:empty3 }
?s ex:x ?x .
stat:p(?x > 0)
```

No `ex:x` → zero rows into `p` → matches/total = 0/0.

> **Result:** NaN

---

## Block — dirty flags & clean baseline (`dirty`)

**Dataset:** [`dirty`](../custom-data/dirty.md)

### Healthy controls (first)

#### `dirtyClean_probability`

Clean vals `10, 20, 30`.  
`p(?val > 15)` → 20 and 30 qualify → 2 out of 3.

> **Result:** `2/3` (`≈ 0.666…`)

#### `dirtyAllTrue_probability`

```sparql
stat:p(?flag = true)   # comparison — the correct shape
```

All flags true → every row matches.

> **Result:** `1.0`

#### `dirtyAllFalse_probability`

All flags false → `?flag = true` never holds.

> **Result:** `0.0`

**Note:** `?flag = true` is still fine; bare `?flag` also works after Kari’s `xsd:boolean` fix.

### Edge cases

#### `dirtyRawBooleanFlag_probability`

```sparql
stat:p(?flag)    # raw Turtle xsd:boolean
```

Raw `xsd:boolean` property is accepted (`CollectorUtil.asBoolean` / Kari `8d15da0`).

> **Result:** `1.0` (allTrue case)

#### `dirtyEmptyCondition_probability`

```sparql
stat:p(?val > 15, ?flag = true)
# emptyCond: flags are false → condition never true
```

Conditional probability with an empty “given” set (like dividing by zero).

> **Result:** NaN

---

## Map of all 10 tests

| Theme | Result |
|-------|--------|
| OPTIONAL | unbound |
| Empty absolute p | NaN |
| Clean / allTrue / allFalse | `2/3`, `1.0`, `0.0` |
| Raw boolean | `1.0` (fixed) |
| Empty condition | NaN |

## Vs the other aggregates

| Problem | Pearson | Cov | Percentile | Probability |
|---------|---------|-----|------------|-------------|
| OPTIONAL blank | unbound | unbound | unbound | unbound |
| Empty input | NaN | 0.0 | NPE | **NaN** |
| Empty *condition* | — | — | — | **NaN** |
| Raw xsd:boolean | — | — | — | **works** (Kari `8d15da0`) |

# 4 — `PercentileEdgeTest.java` (full walkthrough)

Source: [`src/test/java/com/kari3600/me/statistics_rdf4j/PercentileEdgeTest.java`](../src/test/java/com/kari3600/me/statistics_rdf4j/PercentileEdgeTest.java)

> Test method names match the Java `@Test` methods (result words stripped from both).

## What these functions are (kid version)

Imagine lining up all the numbers from smallest to biggest.

- **`stat:percentile(?x, 0.5)`** — “What value sits at the 50% mark?”  
  `0.5` = halfway; `0.9` = “90% of values are below this.”
- **`stat:approx_median(?x)`** — approximate median (same family under the hood).

One column only (`?weight`, `?english`, …) — not a pair.

---

## Class setup

Same `PREFIX` / `DBPEDIA` as the other edge classes.

---

## Block — OPTIONAL on natural + handmade data

**Datasets:** [`cars.ttl`](../src/test/resources/cars.ttl), [`cities.ttl`](../src/test/resources/cities.ttl), [`pairs`](../custom-data/pairs.md)

Same poison rule as pearson: `OPTIONAL` blanks → answer cell blank.

### `carsOptionalMissingWeight_percentile`

`percentile(?weight, 0.5)` with optional weight. Many cars have no weight.

> **Result:** unbound

### `carsOptionalMissingWeight_approxMedian`

Same setup with `approx_median(?weight)`.

> **Result:** unbound

### `citiesOptionalMissingElevation_percentile`

`percentile(?elevation, 0.5)` with optional elevation.

> **Result:** unbound

### `pairsOptionalEnglish_percentile`

Optional english on all students.

> **Result:** unbound

### `pairsOptionalEnglish_approxMedian`

Same with `approx_median`.

> **Result:** unbound

---

## Block — tiny groups (`tiny`)

**Dataset:** [`tiny`](../custom-data/tiny.md)

| case | Role |
|------|------|
| `single` (x=3) | **healthy-ish** — one value is fine for percentile |
| `identical` (all 5s) | **healthy** for percentile (pearson hated this) |
| `empty` | degenerate — no inputs |

### Healthy controls (first)

#### `tinySingle_percentile`

Only one x value: `3`. Middle of a one-item list is that item.

> **Result:** `3.0`

#### `tinyIdentical_percentile`

Three copies of `5`.

> **Result:** `5.0`

### Edge case

#### `tinyEmpty_percentile`

`empty2` / `empty3` have no `ex:x` → zero numbers into the percentile.  
`assertThrows(NullPointerException)` = test **passes** only if that crash happens.

> **Result:** `NullPointerException` (not NaN / not 0)

---

## Block — dirty (`dirty`)

**Dataset:** [`dirty`](../custom-data/dirty.md)

### Healthy control (first)

#### `dirtyDuplicateValues_percentile`

One subject with multi-valued `ex:val` = `1, 1, 2` → three observations. Locked observed median of that bag.

> **Result:** `2.0`

### Edge cases

#### `dirtyStringNumber_percentile`

`stringNum` mixes string `"10"` with number `20`. String isn’t a proper XSD number.

> **Result:** unbound

#### `dirtyPercentileOutOfRange`

```java
stat:percentile(?val, 1.5)   // not in 0..1
assertThrows(RuntimeException.class, ...)
```

> **Result:** throws `RuntimeException`

---

## Map of all 11 tests

| Theme | Result shape |
|-------|--------------|
| OPTIONAL poison | unbound |
| Tiny single / identical | `3.0` / `5.0` |
| Empty input | NPE |
| Dirty multi-value | `2.0` |
| Dirty string | unbound |
| Rank outside 0..1 | throws |

## Vs pearson / covariance

| Input problem | Pearson | Covariance | Percentile |
|---------------|---------|------------|------------|
| OPTIONAL blank | unbound | unbound | unbound |
| Truly empty inputs | NaN | 0.0 | **NPE** |
| Identical values | NaN | 0.0 | **fine** |
| Wrong type / string | unbound | — | unbound |
| Rank outside 0..1 | — | — | throws |

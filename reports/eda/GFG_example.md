# GFG example — EDA

| | |
|---|---|
| **Name** | GFG example |
| **Source file** | [`GFG_example.ttl`](../../src/test/resources/GFG_example.ttl) |
| **Type** | Tiny complete categorical demo |
| **Primary class** | `ex:Person` |
| **Used by** | Happy-path ChiSquaredTest |

---

## Description

Classic gender × ice-cream order contingency table for chi-squared. Fully complete categorical data — contrast with handmade chi fixture that adds gaps.

---

## How many

| | Count |
|---|------:|
| Subjects (`ex:Person`) | **120** |
| Distinct properties seen | 2 |

---

## What kind of values

| Property | Present | Completeness | Kind (observed) |
|----------|--------:|-------------:|-----------------|
| `ex:gender` | 120 | **100.0%** | categorical |
| `ex:order` | 120 | **100.0%** | categorical |

Values for the main pair are **categories** (not continuous measurements).

---

## How complete it is

### Per property (% of 120 subjects)

| Property | Present | Missing | Completeness |
|----------|--------:|--------:|-------------:|
| `ex:gender` | 120 | 0 | **100.0%** |
| `ex:order` | 120 | 0 | **100.0%** |

### Main bivariate pair: `ex:gender` × `ex:order`

| Bucket | Count | Meaning |
|--------|------:|---------|
| **both** | 120 | Has both → survives a REQUIRED join |
| **left_only** | 0 | Has `ex:gender`, missing `ex:order` |
| **right_only** | 0 | Has `ex:order`, missing `ex:gender` |
| **neither** | 0 | Has type but neither property |

**Pair completeness: 100.0%** (120 / 120).

Under REQUIRED patterns, effective n ≈ **120**. Under `OPTIONAL`, incompletes can enter as blanks → many numeric aggregates go **unbound**.

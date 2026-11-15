# Tiny — EDA

| | |
|---|---|
| **Name** | Tiny |
| **Source file** | [`tiny.ttl`](../src/test/resources/custom-data/tiny.ttl) |
| **Table view** | [`custom-data/tiny.md`](../custom-data/tiny.md) |
| **Type** | Handmade edge fixture |
| **Primary class** | `ex:Point` |
| **Used by** | Pearson / covariance / percentile / probability edge tests |

---

## Description

Degenerate / tiny groups sliced by ex:case (empty, single, identical, constantX, two) for NaN / 0 / NPE / healthy-minimal checks.

---

## How many

| | Count |
|---|------:|
| Subjects (`ex:Point`) | **12** |
| Distinct properties seen | 4 |

### Slices

| Value | Subjects with this tag |
|-------|------------------------:|
| `...` | 1 |
| `constantX` | 3 |
| `empty` | 3 |
| `identical` | 3 |
| `single` | 1 |
| `two` | 2 |

---

## What kind of values

| Property | Present | Completeness | Kind (observed) |
|----------|--------:|-------------:|-----------------|
| `ex:case` | 12 | **100.0%** | string |
| `rdfs:label` | 12 | **100.0%** | string |
| `ex:x` | 10 | **83.3%** | numeric |
| `ex:y` | 10 | **83.3%** | numeric |

### Numeric sketch (one value per subject)

| Property | n | min | median | max | Negatives |
|----------|--:|----:|-------:|----:|----------:|
| `ex:x` | 10 | 1 | 2 | 5 | 0 |
| `ex:y` | 10 | 1 | 4 | 7 | 0 |

---

## How complete it is

### Per property (% of 12 subjects)

| Property | Present | Missing | Completeness |
|----------|--------:|--------:|-------------:|
| `ex:case` | 12 | 0 | **100.0%** |
| `rdfs:label` | 12 | 0 | **100.0%** |
| `ex:x` | 10 | 2 | **83.3%** |
| `ex:y` | 10 | 2 | **83.3%** |

### Main bivariate pair: `ex:x` × `ex:y`

| Bucket | Count | Meaning |
|--------|------:|---------|
| **both** | 9 | Has both → survives a REQUIRED join |
| **left_only** | 1 | Has `ex:x`, missing `ex:y` |
| **right_only** | 1 | Has `ex:y`, missing `ex:x` |
| **neither** | 1 | Has type but neither property |

**Pair completeness: 75.0%** (9 / 12).

Under REQUIRED patterns, effective n ≈ **9**. Under `OPTIONAL`, incompletes can enter as blanks → many numeric aggregates go **unbound**.

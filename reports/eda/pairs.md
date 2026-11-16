# Pairs — EDA

| | |
|---|---|
| **Name** | Pairs |
| **Source file** | [`pairs.ttl`](../../src/test/resources/custom-data/pairs.ttl) |
| **Table view** | [`custom-data/pairs.md`](../../custom-data/pairs.md) |
| **Type** | Handmade edge fixture |
| **Primary class** | `ex:Student` |
| **Used by** | Pearson / covariance / percentile / probability edge tests |

---

## Description

Students with math / english scores. Intentionally incomplete pairs (left-only, right-only, neither) so REQUIRED join ≠ OPTIONAL behavior.

---

## How many

| | Count |
|---|------:|
| Subjects (`ex:Student`) | **12** |
| Distinct properties seen | 3 |

---

## What kind of values

| Property | Present | Completeness | Kind (observed) |
|----------|--------:|-------------:|-----------------|
| `rdfs:label` | 12 | **100.0%** | string |
| `ex:math` | 9 | **75.0%** | numeric |
| `ex:english` | 8 | **66.7%** | numeric |

### Numeric sketch (one value per subject)

| Property | n | min | median | max | Negatives |
|----------|--:|----:|-------:|----:|----------:|
| `ex:math` | 9 | 0 | 4 | 10 | 0 |
| `ex:english` | 8 | 0 | 5 | 20 | 0 |

---

## How complete it is

### Per property (% of 12 subjects)

| Property | Present | Missing | Completeness |
|----------|--------:|--------:|-------------:|
| `rdfs:label` | 12 | 0 | **100.0%** |
| `ex:math` | 9 | 3 | **75.0%** |
| `ex:english` | 8 | 4 | **66.7%** |

### Main bivariate pair: `ex:math` × `ex:english`

| Bucket | Count | Meaning |
|--------|------:|---------|
| **both** | 6 | Has both → survives a REQUIRED join |
| **left_only** | 3 | Has `ex:math`, missing `ex:english` |
| **right_only** | 2 | Has `ex:english`, missing `ex:math` |
| **neither** | 1 | Has type but neither property |

**Pair completeness: 50.0%** (6 / 12).

Under REQUIRED patterns, effective n ≈ **6**. Under `OPTIONAL`, incompletes can enter as blanks → many numeric aggregates go **unbound**.

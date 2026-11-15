# Mountains — EDA

| | |
|---|---|
| **Name** | Mountains |
| **Source file** | [`mountains.ttl`](../src/test/resources/mountains.ttl) |
| **Type** | Natural fixture (DBpedia-style scrape) |
| **Primary class** | `dbo:Mountain` |
| **Used by** | Happy-path pearson / covariance / percentile |

---

## Description

Mountains with elevation and prominence — structurally complete main pair. Clean-ish numeric corpus for pearson / percentile without missingness drama.

---

## How many

| | Count |
|---|------:|
| Subjects (`dbo:Mountain`) | **1669** |
| Distinct properties seen | 3 |

---

## What kind of values

| Property | Present | Completeness | Kind (observed) |
|----------|--------:|-------------:|-----------------|
| `dbo:elevation` | 1669 | **100.0%** | numeric |
| `dbo:prominence` | 1669 | **100.0%** | numeric |
| `rdfs:label` | 1669 | **100.0%** | string |

### Numeric sketch (one value per subject)

| Property | n | min | median | max | Negatives |
|----------|--:|----:|-------:|----:|----------:|
| `dbo:elevation` | 1669 | 74 | 2525 | 8586 | 0 |
| `dbo:prominence` | 1669 | 0 | 283 | 6961 | 0 |

---

## How complete it is

### Per property (% of 1669 subjects)

| Property | Present | Missing | Completeness |
|----------|--------:|--------:|-------------:|
| `dbo:elevation` | 1669 | 0 | **100.0%** |
| `dbo:prominence` | 1669 | 0 | **100.0%** |
| `rdfs:label` | 1669 | 0 | **100.0%** |

### Main bivariate pair: `dbo:elevation` × `dbo:prominence`

| Bucket | Count | Meaning |
|--------|------:|---------|
| **both** | 1669 | Has both → survives a REQUIRED join |
| **left_only** | 0 | Has `dbo:elevation`, missing `dbo:prominence` |
| **right_only** | 0 | Has `dbo:prominence`, missing `dbo:elevation` |
| **neither** | 0 | Has type but neither property |

**Pair completeness: 100.0%** (1669 / 1669).

Under REQUIRED patterns, effective n ≈ **1669**. Under `OPTIONAL`, incompletes can enter as blanks → many numeric aggregates go **unbound**.

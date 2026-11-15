# Films — EDA

| | |
|---|---|
| **Name** | Films |
| **Source file** | [`films.ttl`](../films.ttl) |
| **Type** | Natural fixture (DBpedia-style scrape) |
| **Primary class** | `dbo:Film` |
| **Used by** | Happy-path pearson, covariance, percentile (runtime) |

---

## Description

Films with budget and gross (always paired) and runtime often missing. Heavy right skew from blockbusters; good for strong correlation demos and univariate runtime percentiles.

---

## How many

| | Count |
|---|------:|
| Subjects (`dbo:Film`) | **1620** |
| Distinct properties seen | 4 |

---

## What kind of values

| Property | Present | Completeness | Kind (observed) |
|----------|--------:|-------------:|-----------------|
| `dbo:budget` | 1620 | **100.0%** | numeric |
| `dbo:gross` | 1620 | **100.0%** | numeric |
| `rdfs:label` | 1620 | **100.0%** | string |
| `dbo:runtime` | 772 | **47.7%** | numeric |

### Numeric sketch (one value per subject)

| Property | n | min | median | max | Negatives |
|----------|--:|----:|-------:|----:|----------:|
| `dbo:budget` | 1620 | 1 | 5e+06 | 7e+10 | 0 |
| `dbo:gross` | 1620 | 1.15 | 6e+06 | 2.06e+10 | 0 |
| `dbo:runtime` | 772 | 1800 | 6300 | 66180 | 0 |

---

## How complete it is

### Per property (% of 1620 subjects)

| Property | Present | Missing | Completeness |
|----------|--------:|--------:|-------------:|
| `dbo:budget` | 1620 | 0 | **100.0%** |
| `dbo:gross` | 1620 | 0 | **100.0%** |
| `rdfs:label` | 1620 | 0 | **100.0%** |
| `dbo:runtime` | 772 | 848 | **47.7%** |

### Main bivariate pair: `dbo:budget` × `dbo:gross`

| Bucket | Count | Meaning |
|--------|------:|---------|
| **both** | 1620 | Has both → survives a REQUIRED join |
| **left_only** | 0 | Has `dbo:budget`, missing `dbo:gross` |
| **right_only** | 0 | Has `dbo:gross`, missing `dbo:budget` |
| **neither** | 0 | Has type but neither property |

**Pair completeness: 100.0%** (1620 / 1620).

Under REQUIRED patterns, effective n ≈ **1620**. Under `OPTIONAL`, incompletes can enter as blanks → many numeric aggregates go **unbound**.

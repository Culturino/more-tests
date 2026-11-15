# Cars — EDA

| | |
|---|---|
| **Name** | Cars |
| **Source file** | [`cars.ttl`](../src/test/resources/cars.ttl) |
| **Type** | Natural fixture (DBpedia-style scrape) |
| **Primary class** | `dbo:Automobile` |
| **Used by** | Happy-path pearson / covariance / percentile; edge tests for missing weight |

---

## Description

Automobiles with physical measurements and English labels. Main natural fixture for incomplete bivariate pairs: length is always present, weight often missing — good for REQUIRED vs OPTIONAL edge tests.

---

## How many

| | Count |
|---|------:|
| Subjects (`dbo:Automobile`) | **163** |
| Distinct properties seen | 5 |

---

## What kind of values

| Property | Present | Completeness | Kind (observed) |
|----------|--------:|-------------:|-----------------|
| `dbp:length` | 163 | **100.0%** | numeric |
| `dbp:wheelbase` | 163 | **100.0%** | numeric |
| `rdfs:label` | 163 | **100.0%** | string |
| `dbp:width` | 118 | **72.4%** | numeric |
| `dbp:weight` | 60 | **36.8%** | numeric |

### Numeric sketch (one value per subject)

| Property | n | min | median | max | Negatives |
|----------|--:|----:|-------:|----:|----------:|
| `dbp:length` | 163 | -1500 | 1988 | 57900 | 1 |
| `dbp:wheelbase` | 163 | -243 | 1963 | 1.8e+07 | 1 |
| `dbp:width` | 118 | -100 | 301.5 | 16440 | 1 |
| `dbp:weight` | 60 | 2.2 | 1900 | 27000 | 0 |

---

## How complete it is

### Per property (% of 163 subjects)

| Property | Present | Missing | Completeness |
|----------|--------:|--------:|-------------:|
| `dbp:length` | 163 | 0 | **100.0%** |
| `dbp:wheelbase` | 163 | 0 | **100.0%** |
| `rdfs:label` | 163 | 0 | **100.0%** |
| `dbp:width` | 118 | 45 | **72.4%** |
| `dbp:weight` | 60 | 103 | **36.8%** |

### Main bivariate pair: `dbp:length` × `dbp:weight`

| Bucket | Count | Meaning |
|--------|------:|---------|
| **both** | 60 | Has both → survives a REQUIRED join |
| **left_only** | 103 | Has `dbp:length`, missing `dbp:weight` |
| **right_only** | 0 | Has `dbp:weight`, missing `dbp:length` |
| **neither** | 0 | Has type but neither property |

**Pair completeness: 36.8%** (60 / 163).

Under REQUIRED patterns, effective n ≈ **60**. Under `OPTIONAL`, incompletes can enter as blanks → many numeric aggregates go **unbound**.

### Pair: `dbp:length` × `dbp:wheelbase`

| Bucket | Count | Meaning |
|--------|------:|---------|
| **both** | 163 | Has both → survives a REQUIRED join |
| **left_only** | 0 | Has `dbp:length`, missing `dbp:wheelbase` |
| **right_only** | 0 | Has `dbp:wheelbase`, missing `dbp:length` |
| **neither** | 0 | Has type but neither property |

**Pair completeness: 100.0%** (163 / 163).

### Pair: `dbp:length` × `dbp:width`

| Bucket | Count | Meaning |
|--------|------:|---------|
| **both** | 118 | Has both → survives a REQUIRED join |
| **left_only** | 45 | Has `dbp:length`, missing `dbp:width` |
| **right_only** | 0 | Has `dbp:width`, missing `dbp:length` |
| **neither** | 0 | Has type but neither property |

**Pair completeness: 72.4%** (118 / 163).

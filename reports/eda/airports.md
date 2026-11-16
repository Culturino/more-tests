# Airports — EDA

| | |
|---|---|
| **Name** | Airports |
| **Source file** | [`airports.ttl`](../../src/test/resources/airports.ttl) |
| **Type** | Natural fixture (DBpedia-style scrape) |
| **Primary class** | `dbo:Airport` |
| **Used by** | Happy-path pearson / covariance / probability |

---

## Description

Airports with elevation and primary runway length. Main pair is complete; runway length 0 may be a sentinel for unknown rather than a true zero.

---

## How many

| | Count |
|---|------:|
| Subjects (`dbo:Airport`) | **979** |
| Distinct properties seen | 3 |

---

## What kind of values

| Property | Present | Completeness | Kind (observed) |
|----------|--------:|-------------:|-----------------|
| `dbo:elevation` | 979 | **100.0%** | numeric |
| `dbp:r1LengthF` | 979 | **100.0%** | numeric |
| `rdfs:label` | 979 | **100.0%** | string |

### Numeric sketch (one value per subject)

| Property | n | min | median | max | Negatives |
|----------|--:|----:|-------:|----:|----------:|
| `dbo:elevation` | 979 | -378 | 214.9 | 2548 | 4 |
| `dbp:r1LengthF` | 979 | 0 | 4652 | 25000 | 0 |

---

## How complete it is

### Per property (% of 979 subjects)

| Property | Present | Missing | Completeness |
|----------|--------:|--------:|-------------:|
| `dbo:elevation` | 979 | 0 | **100.0%** |
| `dbp:r1LengthF` | 979 | 0 | **100.0%** |
| `rdfs:label` | 979 | 0 | **100.0%** |

### Main bivariate pair: `dbo:elevation` × `dbp:r1LengthF`

| Bucket | Count | Meaning |
|--------|------:|---------|
| **both** | 979 | Has both → survives a REQUIRED join |
| **left_only** | 0 | Has `dbo:elevation`, missing `dbp:r1LengthF` |
| **right_only** | 0 | Has `dbp:r1LengthF`, missing `dbo:elevation` |
| **neither** | 0 | Has type but neither property |

**Pair completeness: 100.0%** (979 / 979).

Under REQUIRED patterns, effective n ≈ **979**. Under `OPTIONAL`, incompletes can enter as blanks → many numeric aggregates go **unbound**.

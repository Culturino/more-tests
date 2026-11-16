# Earthquakes — EDA

| | |
|---|---|
| **Name** | Earthquakes |
| **Source file** | [`earthquakes.ttl`](../../src/test/resources/earthquakes.ttl) |
| **Type** | Natural fixture (DBpedia-style scrape) |
| **Primary class** | `dbo:Earthquake` |
| **Used by** | Happy-path pearson, covariance, percentile, probability |

---

## Description

Earthquake events with casualties and magnitude (plus often lat/long). Main pair is structurally complete, but some numeric values are physically impossible (e.g. negative magnitude).

---

## How many

| | Count |
|---|------:|
| Subjects (`dbo:Earthquake`) | **1069** |
| Distinct properties seen | 3 |

---

## What kind of values

| Property | Present | Completeness | Kind (observed) |
|----------|--------:|-------------:|-----------------|
| `dbp:casualties` | 1069 | **100.0%** | numeric |
| `dbp:magnitude` | 1069 | **100.0%** | numeric |
| `rdfs:label` | 1069 | **100.0%** | string |

### Numeric sketch (one value per subject)

| Property | n | min | median | max | Negatives |
|----------|--:|----:|-------:|----:|----------:|
| `dbp:casualties` | 1069 | -10 | 53 | 260000 | 2 |
| `dbp:magnitude` | 1069 | -10 | 6.8 | 9.4 | 2 |

---

## How complete it is

### Per property (% of 1069 subjects)

| Property | Present | Missing | Completeness |
|----------|--------:|--------:|-------------:|
| `dbp:casualties` | 1069 | 0 | **100.0%** |
| `dbp:magnitude` | 1069 | 0 | **100.0%** |
| `rdfs:label` | 1069 | 0 | **100.0%** |

### Main bivariate pair: `dbp:casualties` × `dbp:magnitude`

| Bucket | Count | Meaning |
|--------|------:|---------|
| **both** | 1069 | Has both → survives a REQUIRED join |
| **left_only** | 0 | Has `dbp:casualties`, missing `dbp:magnitude` |
| **right_only** | 0 | Has `dbp:magnitude`, missing `dbp:casualties` |
| **neither** | 0 | Has type but neither property |

**Pair completeness: 100.0%** (1069 / 1069).

Under REQUIRED patterns, effective n ≈ **1069**. Under `OPTIONAL`, incompletes can enter as blanks → many numeric aggregates go **unbound**.

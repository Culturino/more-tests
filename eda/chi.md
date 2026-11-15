# Chi — EDA

| | |
|---|---|
| **Name** | Chi |
| **Source file** | [`chi.ttl`](../src/test/resources/custom-data/chi.ttl) |
| **Table view** | [`custom-data/chi.md`](../custom-data/chi.md) |
| **Type** | Handmade edge fixture |
| **Primary class** | `ex:Person` |
| **Used by** | ChiSquaredEdgeTest |

---

## Description

Tiny gender × order table plus incomplete persons. Shows chi_sq treating missing OPTIONAL args as null categories (still a number).

---

## How many

| | Count |
|---|------:|
| Subjects (`ex:Person`) | **7** |
| Distinct properties seen | 3 |

---

## What kind of values

| Property | Present | Completeness | Kind (observed) |
|----------|--------:|-------------:|-----------------|
| `rdfs:label` | 7 | **100.0%** | string |
| `ex:gender` | 5 | **71.4%** | categorical |
| `ex:order` | 5 | **71.4%** | categorical |

Values for the main pair are **categories** (not continuous measurements).

---

## How complete it is

### Per property (% of 7 subjects)

| Property | Present | Missing | Completeness |
|----------|--------:|--------:|-------------:|
| `rdfs:label` | 7 | 0 | **100.0%** |
| `ex:gender` | 5 | 2 | **71.4%** |
| `ex:order` | 5 | 2 | **71.4%** |

### Main bivariate pair: `ex:gender` × `ex:order`

| Bucket | Count | Meaning |
|--------|------:|---------|
| **both** | 4 | Has both → survives a REQUIRED join |
| **left_only** | 1 | Has `ex:gender`, missing `ex:order` |
| **right_only** | 1 | Has `ex:order`, missing `ex:gender` |
| **neither** | 1 | Has type but neither property |

**Pair completeness: 57.1%** (4 / 7).

Under REQUIRED patterns, effective n ≈ **4**. Under `OPTIONAL`, incompletes can enter as blanks → many numeric aggregates go **unbound**.

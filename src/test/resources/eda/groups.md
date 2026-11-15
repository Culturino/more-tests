# Groups — EDA

| | |
|---|---|
| **Name** | Groups |
| **Source file** | [`groups.ttl`](../custom-data/groups.ttl) |
| **Table view** | [`custom-data/groups.md`](../../../../custom-data/groups.md) |
| **Type** | Handmade edge fixture |
| **Primary class** | `ex:Student` |
| **Used by** | PearsonEdgeTest richer queries |

---

## Description

Students in groups A–D for GROUP BY / FILTER / multi-aggregate edge tests (healthy line, single pair, identical, incompletes-only).

---

## How many

| | Count |
|---|------:|
| Subjects (`ex:Student`) | **9** |
| Distinct properties seen | 4 |

### Slices

| Value | Subjects with this tag |
|-------|------------------------:|
| `A` | 4 |
| `B` | 1 |
| `C` | 2 |
| `D` | 2 |

---

## What kind of values

| Property | Present | Completeness | Kind (observed) |
|----------|--------:|-------------:|-----------------|
| `ex:group` | 9 | **100.0%** | string |
| `rdfs:label` | 9 | **100.0%** | string |
| `ex:english` | 8 | **88.9%** | numeric |
| `ex:math` | 8 | **88.9%** | numeric |

### Numeric sketch (one value per subject)

| Property | n | min | median | max | Negatives |
|----------|--:|----:|-------:|----:|----------:|
| `ex:math` | 8 | 1 | 4.5 | 9 | 0 |
| `ex:english` | 8 | 1 | 4.5 | 8 | 0 |

---

## How complete it is

### Per property (% of 9 subjects)

| Property | Present | Missing | Completeness |
|----------|--------:|--------:|-------------:|
| `ex:group` | 9 | 0 | **100.0%** |
| `rdfs:label` | 9 | 0 | **100.0%** |
| `ex:english` | 8 | 1 | **88.9%** |
| `ex:math` | 8 | 1 | **88.9%** |

### Main bivariate pair: `ex:math` × `ex:english`

| Bucket | Count | Meaning |
|--------|------:|---------|
| **both** | 7 | Has both → survives a REQUIRED join |
| **left_only** | 1 | Has `ex:math`, missing `ex:english` |
| **right_only** | 1 | Has `ex:english`, missing `ex:math` |
| **neither** | 0 | Has type but neither property |

**Pair completeness: 77.8%** (7 / 9).

Under REQUIRED patterns, effective n ≈ **7**. Under `OPTIONAL`, incompletes can enter as blanks → many numeric aggregates go **unbound**.

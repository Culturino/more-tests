# Dirty — EDA

| | |
|---|---|
| **Name** | Dirty |
| **Source file** | [`dirty.ttl`](../../src/test/resources/custom-data/dirty.ttl) |
| **Table view** | [`custom-data/dirty.md`](../../custom-data/dirty.md) |
| **Type** | Handmade edge fixture |
| **Primary class** | `ex:Item` |
| **Used by** | Percentile / probability / pearson dirty edge tests |

---

## Description

Dirty / weird values: string-as-number, mixed types, boolean flags, multi-valued properties, empty conditional probability cases.

---

## How many

| | Count |
|---|------:|
| Subjects (`ex:Item`) | **15** |
| Distinct properties seen | 5 |

### Slices

| Value | Subjects with this tag |
|-------|------------------------:|
| `allFalse` | 2 |
| `allTrue` | 2 |
| `clean` | 3 |
| `dup` | 1 |
| `emptyCond` | 2 |
| `mixed` | 3 |
| `stringNum` | 2 |

---

## What kind of values

| Property | Present | Completeness | Kind (observed) |
|----------|--------:|-------------:|-----------------|
| `ex:case` | 15 | **100.0%** | string |
| `ex:val` | 11 | **73.3%** | numeric, other, string |
| `ex:flag` | 9 | **60.0%** | boolean |
| `rdfs:label` | 9 | **60.0%** | string |
| `ex:other` | 3 | **20.0%** | numeric |

### Numeric sketch (one value per subject)

| Property | n | min | median | max | Negatives |
|----------|--:|----:|-------:|----:|----------:|
| `ex:val` | 10 | 1 | 10 | 30 | 0 |
| `ex:other` | 3 | 2 | 4 | 6 | 0 |

---

## How complete it is

### Per property (% of 15 subjects)

| Property | Present | Missing | Completeness |
|----------|--------:|--------:|-------------:|
| `ex:case` | 15 | 0 | **100.0%** |
| `ex:val` | 11 | 4 | **73.3%** |
| `ex:flag` | 9 | 6 | **60.0%** |
| `rdfs:label` | 9 | 6 | **60.0%** |
| `ex:other` | 3 | 12 | **20.0%** |

### Main bivariate pair: `ex:val` × `ex:other`

| Bucket | Count | Meaning |
|--------|------:|---------|
| **both** | 3 | Has both → survives a REQUIRED join |
| **left_only** | 8 | Has `ex:val`, missing `ex:other` |
| **right_only** | 0 | Has `ex:other`, missing `ex:val` |
| **neither** | 4 | Has type but neither property |

**Pair completeness: 20.0%** (3 / 15).

Under REQUIRED patterns, effective n ≈ **3**. Under `OPTIONAL`, incompletes can enter as blanks → many numeric aggregates go **unbound**.

# 3 — `CovarianceEdgeTest.java` (full walkthrough)

Source: [`src/test/java/com/kari3600/me/statistics_rdf4j/CovarianceEdgeTest.java`](../src/test/java/com/kari3600/me/statistics_rdf4j/CovarianceEdgeTest.java)

> Test method names match the Java `@Test` methods (result words stripped from both).

## Pearson vs covariance (kid version)

- **Pearson correlation** — “How tightly do X and Y move together?” (−1…1)
- **Covariance** — “Do they move together, and by how much in raw units?” (any size)

Same query shapes as pearson. Different function (`stat:covariance`) and different degenerate answers.

| Situation | Pearson | Covariance |
|-----------|---------|------------|
| OPTIONAL + blanks | unbound | unbound |
| REQUIRED healthy data | float (e.g. 1.0) | float (e.g. 25.83) |
| Tiny / degenerate | **NaN** | **0.0** |
| Minimal healthy `two` | 1.0 | 1.0 |

---

## Class setup

Same `PREFIX` / `DBPEDIA` as pearson.

### Helper — `assertTinyCovariance(caseName)`

```java
assertEquals(0.0, query1Group1Var(... stat:covariance(?x,?y) ...).doubleValue(), 1e-12);
```

For degenerate slices, sample covariance comes back as **0.0**, not NaN.

---

## Block — `pairs`

**Dataset:** [`pairs`](../custom-data/pairs.md) (same students as pearson)

### Healthy controls (first)

#### `requiredPerfectLine_covariance`

Cara/dan/ella/finn, both scores required.  
Points `(2,4)(3,6)(4,8)(10,20)`.  
Cov isn’t scaled like correlation — not 1.0.

> **Result:** `≈ 25.833333333333332`

#### `requiredAllComplete_covariance`

All students with both scores (includes iris/jake).

> **Result:** `≈ 9.933333333333334`

### Edge cases

#### `optionalAllYMissing_covariance`

Alice + bob, math required, english `OPTIONAL` → blanks in.

> **Result:** unbound

#### `optionalMixed_covariance`

All students with math; english optional → one blank ruins the barrel.

> **Result:** unbound

---

## Block — cars / cities

**Datasets:** [`cars.ttl`](../src/test/resources/cars.ttl), [`cities.ttl`](../src/test/resources/cities.ttl)

### Healthy control (first)

#### `carsRequiredComplete_covariance`

REQUIRED length + weight (incompletes dropped). Tolerance `1.0` because the number is huge.

> **Result:** `≈ 21770421.170564972`

### Edge cases

#### `carsOptionalMissingWeight_covariance`

Required length, optional weight.

> **Result:** unbound

#### `citiesOptionalMissingElevation_covariance`

Required population, optional elevation.

> **Result:** unbound

---

## Block — tiny (`tiny`)

**Dataset:** [`tiny`](../custom-data/tiny.md)

| case | Role |
|------|------|
| `two` `(1,2)(2,4)` | **healthy control** |
| empty / single / identical / constantX | degenerate |

### Healthy control (first)

#### `tinyTwo_covariance`

Smallest valid line.

> **Result:** `1.0`

### Edge cases

#### `tinyEmpty_covariance`

> **Result:** `0.0`

#### `tinySingle_covariance`

> **Result:** `0.0`

#### `tinyIdentical_covariance`

> **Result:** `0.0`

#### `tinyConstantX_covariance`

> **Result:** `0.0`

---

## Map of all 12 tests

| Theme | Tests |
|-------|--------|
| Healthy / REQUIRED | perfect line, all complete, cars required, tiny two |
| OPTIONAL | all Y missing, mixed, cars optional, cities optional |
| Tiny degenerate | empty, single, identical, constantX → 0.0 |

No `GROUP BY` / dirty / richer tests in this file.

# 6 — `ChiSquaredEdgeTest.java` (full walkthrough)

Source: [`src/test/java/com/kari3600/me/statistics_rdf4j/ChiSquaredEdgeTest.java`](../../src/test/java/com/kari3600/me/statistics_rdf4j/ChiSquaredEdgeTest.java)

> Test method names match the Java `@Test` methods (result words stripped from both).

**Dataset:** [`chi`](../../custom-data/chi.md)

## What `stat:chi_sq` is (kid version)

Chi-squared asks: **“Are these two categories independent, or linked?”**

Example: gender × ice-cream order.  
If men and women pick flavors the same way → near **0**.  
If the pattern is weird/uneven → number gets **bigger**.

Works on **labels** (Male/Female, Chocolate/Vanilla), not on numeric size.

---

## The twist vs everything else

| Aggregate | OPTIONAL missing value |
|-----------|-------------------------|
| pearson / covariance / percentile / probability | **unbound** |
| **`chi_sq`** | still a **number** — blank = its own “null” category |

This file never calls `assertUnboundAggregate`.

---

## Prefix note

```java
private static final String CHI_PREFIX = """
        PREFIX ex: <http://example.org/>
        """;
```

Slightly different base URL than other edge fixtures (`http://example.org/` vs `.../edge#`). Matches `chi.ttl`.

---

## Who’s in the data

| Person | gender | order | Role |
|--------|--------|-------|------|
| A | Male | Chocolate | **healthy complete** |
| B | Male | Vanilla | **healthy complete** |
| C | Female | Chocolate | **healthy complete** |
| D | Female | Vanilla | **healthy complete** |
| E | Male | — | incomplete |
| F | — | Strawberry | incomplete |
| G | — | — | incomplete |

A–D = balanced 2×2. E–G = incompletes on purpose.

---

## Healthy control (first)

### `chiRequiredComplete`

```sparql
?person a ex:Person ;
    ex:gender ?gender ;
    ex:order ?order .
```

- REQUIRED both → only A–D (E/F/G dropped).
- Perfect balanced 2×2 (one of each gender×flavor).
- Looks independent.

> **Result:** `0.0`

---

## Edge cases

### `chiOptionalMissing`

```sparql
SELECT (stat:chi_sq(?gender, ?order) AS ?chi)
WHERE {
    ?person a ex:Person .
    OPTIONAL { ?person ex:gender ?gender }
    OPTIONAL { ?person ex:order ?order }
}
```

- All 7 people enter.
- Missing gender/order count as **null categories**, not poison.
- If this were pearson, the same OPTIONAL pattern would blank the answer.

> **Result:** `5.25`

### `chiOnlyIncompletes`

```sparql
VALUES ?person { ex:PersonE ex:PersonF ex:PersonG }
... OPTIONAL gender ... OPTIONAL order ...
```

Only the broken three. Nulls are just more category buckets.

> **Result:** `0.75`

---

## Map of all 3 tests

| Test | Who | Pattern | Result |
|------|-----|---------|--------|
| required complete | A–D | REQUIRED | **0.0** |
| optional missing | all 7 | OPTIONAL | **5.25** |
| only incompletes | E,F,G | OPTIONAL | **0.75** |

## Takeaway

`chi_sq` is the odd one out: **missing ≠ unbound answer**.  
Missing becomes another category, and you still get a number.

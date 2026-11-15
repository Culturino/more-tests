# more-tests (statistics-rdf4j)

**SPARQL statistics aggregates for [Eclipse RDF4J](https://rdf4j.org/)**, plus an expanded test suite (natural DBpedia-style fixtures and dedicated edge-case coverage).

| | |
|---|---|
| **GitHub** | https://github.com/Culturino/more-tests |
| **Branch** | `develop` |
| **Language** | Java 25 |
| **Build** | Maven |
| **RDF4J (this checkout)** | `6.2.0-M2` (local/runnable) |

For a deep dive into edge-case findings, see **[EDGE_CASE_REPORT.md](EDGE_CASE_REPORT.md)**.

Handmade edge fixtures live in **[`custom-data/`](custom-data/)** (see [table below](#71-custom-data-handmade-edge-fixtures)).

---

## Table of contents

1. [What is this?](#1-what-is-this)
2. [Why it exists](#2-why-it-exists)
3. [Mental model (simple)](#3-mental-model-simple)
4. [Statistics functions](#4-statistics-functions)
5. [Project layout](#5-project-layout)
6. [How a query runs](#6-how-a-query-runs)
7. [Datasets](#7-datasets)
8. [Test suite](#8-test-suite)
9. [Edge-case agenda (four points)](#9-edge-case-agenda-four-points)
10. [Build & run](#10-build--run)
11. [Writing a new test](#11-writing-a-new-test)
12. [Dependencies & local notes](#12-dependencies--local-notes)
13. [Related docs](#13-related-docs)

---

## 1. What is this?

**RDF** stores facts as a graph of triples:

```text
subject  —property→  value
Car#Audi  —weight→   1400
```

**SPARQL** is the query language for that graph (SQL-like, but for graphs).

Stock SPARQL can `COUNT`, `SUM`, `AVG`, etc. This library plugs **custom aggregate functions** into RDF4J so you can write queries like:

```sparql
PREFIX stat: <http://me.kari3600.com/statistics-rdf4j#>
PREFIX dbo:  <http://dbpedia.org/ontology/>
PREFIX dbp:  <http://dbpedia.org/property/>

SELECT (stat:pearson(?length, ?weight) AS ?corr)
WHERE {
  ?car a dbo:Automobile ;
       dbp:length ?length ;
       dbp:weight ?weight .
}
```

That asks: *across cars that have both length and weight, how strongly do those two numbers move together?*

---

## 2. Why it exists

This repo packages the statistics aggregate implementations and emphasizes:

1. **Running and verifying** those functions against realistic Turtle (`.ttl`) fixtures.
2. Going beyond “one happy `SELECT` with two columns” — **edge cases**:
   - missing / unbound values
   - tiny or empty groups
   - richer SPARQL (`GROUP BY`, `FILTER`, multi-aggregate)
   - dirty / weird typed data
3. Documenting **observed behavior** (unbound, `NaN`, NPE, etc.) so reviewers know what the library actually does today.

---

## 3. Mental model (simple)

Think of two spreadsheet columns side by side:

| car | length | weight |
|-----|--------|--------|
| A   | 4000   | 1400   |
| B   | 3800   | *(missing)* |
| C   | 4200   | 1500   |

**If the query requires both length and weight**, car B never enters the stats function — the join drops it.

**If the query uses `OPTIONAL` for weight**, car B still appears with weight blank (unbound). Then the aggregate must decide what to do. In this library, for most numeric stats, that usually **poisons** the result → the answer variable comes back **unbound** (empty), not a useful number.

There is no JSON `null` in SPARQL; “null” means **unbound** or **missing triple**.

---

## 4. Statistics functions

Namespace prefix used in tests:

```sparql
PREFIX stat: <http://me.kari3600.com/statistics-rdf4j#>
```

| SPARQL name | Arity | Meaning (plain language) |
|-------------|-------|---------------------------|
| `stat:pearson` | 2 | Sample Pearson correlation (−1…1-ish): do two numeric series move together? |
| `stat:pearson_population` | 2 | Population variant of pearson |
| `stat:covariance` | 2 | Sample covariance (same idea as correlation, not scaled to −1…1) |
| `stat:covariance_population` | 2 | Population covariance |
| `stat:percentile` | 2 | Approximate percentile via T-Digest: `(valueVar, p)` with `p` in `[0, 1]` |
| `stat:approx_median` | 1 | Approximate median (T-Digest) |
| `stat:p` | 1 | Probability / fraction of rows where a boolean expression is true |
| `stat:p` | 2 | Conditional probability: `p(event, condition)` |
| `stat:chi_sq` | 2+ | Chi-squared style association over categorical bindings |

Factories are registered via Java SPI under:

- [`AggregateFunctionFactory`](src/main/resources/META-INF/services/org.eclipse.rdf4j.query.parser.sparql.aggregate.AggregateFunctionFactory)
- [`AggregateNAryFunctionFactory`](src/main/resources/META-INF/services/org.eclipse.rdf4j.query.parser.sparql.aggregate.AggregateNAryFunctionFactory)

---

## 5. Project layout

```text
more-tests/
├── README.md
├── EDGE_CASE_REPORT.md
├── custom-data/              ← handmade edge fixtures (see §7.1)
├── pom.xml
└── src/
    ├── main/java/.../statistics_rdf4j/
    │   ├── correlation/pearson/
    │   ├── covariance/
    │   ├── percentile/
    │   ├── median/
    │   ├── probability/
    │   ├── chi_squared/
    │   └── … shared aggregate plumbing …
    ├── main/resources/META-INF/services/
    └── test/
        ├── java/.../EdgeCaseTest.java, PearsonTest.java, …
        └── resources/          ← natural DBpedia-style TTLs
```

Maven copies [`custom-data/`](custom-data/) onto the test classpath as `custom-data/*` (see [`pom.xml`](pom.xml) `testResources`).

---

## 6. How a query runs

```text
TTL file  →  MemoryStore repository  →  SPARQL with stat:* aggregates
                                              ↓
                                    factories (SPI) build collectors
                                              ↓
                                    each solution row → addValue(...)
                                              ↓
                                    getFinalValue() → literal or type-error
                                              ↓
                                    JUnit asserts the binding
```

[`TestUtil.java`](src/test/java/com/kari3600/me/statistics_rdf4j/TestUtil.java):

- Loads a dataset from the test classpath into an in-memory RDF4J sail.
- Prepends the `stat:` prefix.
- Evaluates a tuple query.
- Helpers:
  - `query1Group1Var` — one row, one bound literal (happy path).
  - `queryAll` — all rows (unbound / multi-group asserts).

Numeric conversion goes through `CollectorUtil.asDouble`. Bad or unbound values raise a type error; for most aggregates that surfaces as an **unbound** result variable.

---

## 7. Datasets

### 7.1 Custom data (handmade edge fixtures)

All purpose-built edge datasets are in [`custom-data/`](custom-data/):

| File | Agenda | Purpose |
|------|--------|---------|
| [`edge_pairs.ttl`](custom-data/edge_pairs.ttl) | #1 | Students with `math` / `english`; one-sided and both-missing rows (incomplete-pair / “null” case) |
| [`edge_chi.ttl`](custom-data/edge_chi.ttl) | #1 | Tiny gender × order table plus incomplete persons for `chi_sq` |
| [`edge_tiny.ttl`](custom-data/edge_tiny.ttl) | #2 | `ex:case` slices: empty / single / identical / constantX / two |
| [`edge_groups.ttl`](custom-data/edge_groups.ttl) | #3 | Groups A–D for `GROUP BY`, empty `FILTER`, multi-aggregate |
| [`edge_dirty.ttl`](custom-data/edge_dirty.ttl) | #4 | Strings-as-numbers, mixed types, flags, duplicates, empty condition |

In tests, load them as classpath paths, e.g. `"custom-data/edge_pairs.ttl"`.

### 7.2 Natural / DBpedia-style fixtures

Under [`src/test/resources/`](src/test/resources/), used by happy-path tests:

| File | Domain (approx.) | Typical variables |
|------|------------------|-------------------|
| [`earthquakes.ttl`](src/test/resources/earthquakes.ttl) | Earthquakes | casualties, magnitude |
| [`cars.ttl`](src/test/resources/cars.ttl) | Automobiles | length, weight, … *(many length-without-weight)* |
| [`films.ttl`](src/test/resources/films.ttl) | Films | budget, gross, runtime |
| [`cities.ttl`](src/test/resources/cities.ttl) | Cities | population, elevation, area *(many missing elevation)* |
| [`mountains.ttl`](src/test/resources/mountains.ttl) | Mountains | elevation, prominence |
| [`countries.ttl`](src/test/resources/countries.ttl) | Countries | HDI, Gini |
| [`airports.ttl`](src/test/resources/airports.ttl) | Airports | elevation, runway length |
| [`universities.ttl`](src/test/resources/universities.ttl) | Universities | students, endowment |
| [`GFG_example.ttl`](src/test/resources/GFG_example.ttl) | Tiny chi-squared demo | gender × ice-cream order |

These graphs are **imperfect on purpose**. Happy-path queries usually **require** both variables, so incompletes are silently dropped. [`EdgeCaseTest`](src/test/java/com/kari3600/me/statistics_rdf4j/EdgeCaseTest.java) also reuses [`cars.ttl`](src/test/resources/cars.ttl) and [`cities.ttl`](src/test/resources/cities.ttl) via `EDGE_*` methods with `OPTIONAL`.

---

## 8. Test suite

| Class | Focus | ~Tests |
|-------|--------|--------|
| [`PearsonTest`](src/test/java/com/kari3600/me/statistics_rdf4j/PearsonTest.java) | Happy-path correlation on natural TTLs | 8 |
| [`CovarianceTest`](src/test/java/com/kari3600/me/statistics_rdf4j/CovarianceTest.java) | Happy-path covariance | 6 |
| [`PercentileTest`](src/test/java/com/kari3600/me/statistics_rdf4j/PercentileTest.java) | Happy-path percentiles | 5 |
| [`ProbabilityTest`](src/test/java/com/kari3600/me/statistics_rdf4j/ProbabilityTest.java) | Happy-path `p` / conditional `p` | 7 |
| [`ChiSquaredTest`](src/test/java/com/kari3600/me/statistics_rdf4j/ChiSquaredTest.java) | GFG example | 1 |
| [`EdgeCaseTest`](src/test/java/com/kari3600/me/statistics_rdf4j/EdgeCaseTest.java) | Agendas #1–#4 + `EDGE_*` on cars/cities | 52 |

**Naming:** edge methods on natural data start with `EDGE_` (e.g. `EDGE_carsOptionalMissingWeight_pearson`).

```bash
mvn test
mvn test -Dtest=EdgeCaseTest
```

---

## 9. Edge-case agenda (four points)

Full narrative: **[EDGE_CASE_REPORT.md](EDGE_CASE_REPORT.md)**.

### Agenda #1 — Incomplete / null pairs

**Question:** What if we force missing values into the aggregate with `OPTIONAL`?

**Example:** correlate math `[0, 1]` with english missing ([`edge_pairs.ttl`](custom-data/edge_pairs.ttl) alice & bob).

| Behavior | Result |
|----------|--------|
| Most numeric aggregates + unbound arg | **Unbound** answer (empty) |
| One unbound row in a mixed group | **Poisons** the whole group |
| Required patterns | Incompletes **dropped**; known floats on the complete subset |
| `chi_sq` + OPTIONAL ([`edge_chi.ttl`](custom-data/edge_chi.ttl)) | Still returns a **number** |

Also covered on [`cars.ttl`](src/test/resources/cars.ttl) / [`cities.ttl`](src/test/resources/cities.ttl) via `EDGE_*`.

### Agenda #2 — Tiny / empty groups

Dataset: [`edge_tiny.ttl`](custom-data/edge_tiny.ttl).

| Slice | pearson | covariance | notes |
|-------|---------|------------|--------|
| empty / single / identical / constantX | **NaN** | **0.0** | degenerate |
| empty percentile (no inputs) | — | — | **NPE** |
| empty probability | — | — | **NaN** (0/0) |
| `two` (minimal healthy line) | **1** | **1** | sanity check |

### Agenda #3 — Richer queries

Dataset: [`edge_groups.ttl`](custom-data/edge_groups.ttl).

1. **`GROUP BY ?g`** with pearson + `COUNT(*)` — A→1.0, B/C→NaN, D absent  
2. **`FILTER` that removes all rows** — pearson → NaN  
3. **Multi-aggregate SELECT** — pearson + covariance + count together  

### Agenda #4 — Dirty / weird values

Dataset: [`edge_dirty.ttl`](custom-data/edge_dirty.ttl).

| Input | Observed |
|-------|----------|
| String `"10"` / `"oops"` in numeric stats | **Unbound** |
| `stat:p(?flag)` raw xsd:boolean | **Unbound** (use `?flag = true`) |
| All true / all false with `?flag = true` | **1.0** / **0.0** |
| Conditional `p` with empty condition set | **NaN** |
| Percentile rank `1.5` | **Throws** |
| Multi-valued property `1,1,2` | Percentile runs on three observations |

---

## 10. Build & run

### Requirements

- **JDK 25**
- **Maven 3.9+**
- Network (or populated local `.m2`) for dependencies

```bash
mvn test
mvn test -Dtest=EdgeCaseTest
mvn test -Dtest=EdgeCaseTest#optionalAllYMissing_pearson
```

---

## 11. Writing a new test

### Happy-path

```java
assertEquals(expected, query1Group1Var("cars.ttl", """
        PREFIX dbo: <http://dbpedia.org/ontology/>
        PREFIX dbp: <http://dbpedia.org/property/>
        SELECT (stat:pearson(?length, ?weight) AS ?corr)
        WHERE {
            ?car a dbo:Automobile ;
                dbp:length ?length ;
                dbp:weight ?weight .
        }
        """, "corr").doubleValue(), 0.001);
```

### Edge (unbound) on custom data

```java
assertUnboundAggregate("custom-data/edge_pairs.ttl", PREFIX + """
        SELECT (stat:pearson(?math, ?english) AS ?corr)
        WHERE {
            ?student ex:math ?math .
            OPTIONAL { ?student ex:english ?english }
        }
        """, "corr");
```

### Tips

1. Prefer a new file under [`custom-data/`](custom-data/) for a controlled shape.
2. Reuse natural TTLs with an `EDGE_` name when the mess already exists.
3. Lock expecteds to **observed** behavior.
4. Compute expected floats independently on tiny sets.

---

## 12. Dependencies & local notes

| Dependency | Role |
|------------|------|
| `org.eclipse.rdf4j:*-6.2.0-M2` | RDF4J APIs + in-memory sail for tests |
| `commons-math3` | Multivariate summary stats (pearson / covariance) |
| `datasketches-java` | T-Digest for approx percentile / median |
| `com.kari3600.me:Utils` | Combinatorics helper used by chi-squared |
| JUnit 5 | Tests |

This checkout targets **published RDF4J `6.2.0-M2`**: n-ary factories use `getMin/MaxNumberOfArguments()` so tests run without a private SNAPSHOT build. The `Utils` artifact must be present in the local Maven repository.

---

## 13. Related docs

| Doc | Contents |
|-----|----------|
| [EDGE_CASE_REPORT.md](EDGE_CASE_REPORT.md) | Full write-up of agendas #1–#4 |
| [custom-data/](custom-data/) | Handmade edge TTL fixtures |
| [README.md](README.md) | This overview |

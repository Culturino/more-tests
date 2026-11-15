# more-tests (statistics-rdf4j)

**SPARQL statistics aggregates for [Eclipse RDF4J](https://rdf4j.org/)**, plus an expanded test suite (natural DBpedia-style fixtures and dedicated edge-case coverage).

This repository is the **Culturino** working copy / test-focused fork of the statistics library originally developed under [Kari3600/statistics-rdf4j](https://github.com/Kari3600/statistics-rdf4j).

| | |
|---|---|
| **GitHub** | https://github.com/Culturino/more-tests |
| **Branch** | `develop` |
| **Language** | Java 25 |
| **Build** | Maven |
| **RDF4J (this checkout)** | `6.2.0-M2` (local/runnable) |

For a deep dive into edge-case findings only, see **[EDGE_CASE_REPORT.md](EDGE_CASE_REPORT.md)**.

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

Upstream work adds the aggregate implementations. This repo emphasizes:

1. **Running and verifying** those functions against realistic Turtle (`.ttl`) fixtures.
2. Going beyond “one happy `SELECT` with two columns” — **edge cases** Kari asked for:
   - missing / unbound values
   - tiny or empty groups
   - richer SPARQL (`GROUP BY`, `FILTER`, multi-aggregate)
   - dirty / weird typed data
3. Documenting **observed behavior** (unbound, `NaN`, NPE, etc.) so reviewers know what the library actually does today.

---

## 3. Mental model (simple)

Think of two spreadsheets columns side by side:

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

- `META-INF/services/org.eclipse.rdf4j.query.parser.sparql.aggregate.AggregateFunctionFactory`
- `META-INF/services/org.eclipse.rdf4j.query.parser.sparql.aggregate.AggregateNAryFunctionFactory`

---

## 5. Project layout

```text
more-tests/
├── README.md                 ← this file
├── EDGE_CASE_REPORT.md       ← detailed edge-case findings
├── pom.xml
└── src/
    ├── main/java/.../statistics_rdf4j/
    │   ├── correlation/pearson/     # pearson collectors + factories
    │   ├── covariance/              # covariance
    │   ├── percentile/              # approx percentile (T-Digest)
    │   ├── median/                  # approx median
    │   ├── probability/             # p / conditional p
    │   ├── chi_squared/             # chi_sq
    │   ├── SimpleAggregate*.java    # shared aggregate plumbing
    │   ├── MultivariateStatisticCollector.java
    │   ├── ErrorHoldingAggregateCollector.java
    │   └── CollectorUtil.java       # asDouble / asBoolean
    ├── main/resources/META-INF/services/   # RDF4J SPI registration
    └── test/
        ├── java/.../
        │   ├── TestUtil.java        # load TTL + run SPARQL
        │   ├── PearsonTest.java     # happy-path pearson
        │   ├── CovarianceTest.java
        │   ├── PercentileTest.java
        │   ├── ProbabilityTest.java
        │   ├── ChiSquaredTest.java
        │   └── EdgeCaseTest.java    # agendas #1–#4
        └── resources/
            ├── earthquakes.ttl, cars.ttl, films.ttl, ...
            └── edge_pairs.ttl, edge_tiny.ttl, ...
```

---

## 6. How a query runs

High-level pipeline:

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

`TestUtil`:

- Loads `/dataset.ttl` from test resources into an in-memory RDF4J sail.
- Prepends the `stat:` prefix.
- Evaluates a tuple query.
- Helpers:
  - `query1Group1Var` — expect one row, one bound literal (happy path).
  - `queryAll` — collect all rows (needed for unbound / multi-group asserts).

Inside collectors, numeric conversion goes through `CollectorUtil.asDouble`. A bad or unbound value raises `ValueExprEvaluationException`; `SimpleAggregateNAryFunction` stores that as a **type error**. On finalize, RDF4J typically surfaces that as an **unbound** aggregate variable rather than always throwing from the test helper.

---

## 7. Datasets

### 7.1 Natural / DBpedia-style fixtures

Used by happy-path tests (`PearsonTest`, `CovarianceTest`, …):

| File | Domain (approx.) | Typical variables |
|------|------------------|-------------------|
| `earthquakes.ttl` | Earthquakes | casualties, magnitude |
| `cars.ttl` | Automobiles | length, weight, … *(many length-without-weight)* |
| `films.ttl` | Films | budget, gross, runtime |
| `cities.ttl` | Cities | population, elevation, area *(many missing elevation)* |
| `mountains.ttl` | Mountains | elevation, prominence |
| `countries.ttl` | Countries | HDI, Gini |
| `airports.ttl` | Airports | elevation, runway length |
| `universities.ttl` | Universities | students, endowment |
| `GFG_example.ttl` | Tiny chi-squared demo | gender × ice-cream order |

These graphs are **imperfect on purpose** (missing properties, messy literals). Happy-path queries usually **require** both variables, so incompletes are silently dropped.

### 7.2 Handmade edge fixtures

| File | Agenda | Purpose |
|------|--------|---------|
| `edge_pairs.ttl` | #1 | Students; math/english with one-sided / both-missing rows (Kari null case) |
| `edge_chi.ttl` | #1 | Tiny categorical table + incomplete persons for `chi_sq` |
| `edge_tiny.ttl` | #2 | `ex:case` slices: empty / single / identical / constantX / two |
| `edge_groups.ttl` | #3 | Groups A–D for `GROUP BY`, FILTER, multi-aggregate |
| `edge_dirty.ttl` | #4 | Strings-as-numbers, mixed types, flags, duplicates, empty condition |

---

## 8. Test suite

Approximate counts (current checkout):

| Class | Focus | ~Tests |
|-------|--------|--------|
| `PearsonTest` | Happy-path correlation on natural TTLs | 8 |
| `CovarianceTest` | Happy-path covariance | 6 |
| `PercentileTest` | Happy-path percentiles | 5 |
| `ProbabilityTest` | Happy-path `p` / conditional `p` | 7 |
| `ChiSquaredTest` | GFG example | 1 |
| `EdgeCaseTest` | Agendas #1–#4 + `EDGE_*` on cars/cities | 52 |

**Naming convention for edge tests on natural data:** methods start with `EDGE_` (e.g. `EDGE_carsOptionalMissingWeight_pearson`) so they stand out next to happy-path clones.

Run everything:

```bash
mvn test
```

Run only edge cases:

```bash
mvn test -Dtest=EdgeCaseTest
```

---

## 9. Edge-case agenda (four points)

This is the coverage Kari requested beyond 1:1 happy-path copies. Full narrative: [EDGE_CASE_REPORT.md](EDGE_CASE_REPORT.md).

### Agenda #1 — Incomplete / null pairs

**Question:** What if we force missing values into the aggregate with `OPTIONAL`?

**Example (Kari):** correlate math `[0, 1]` with english `[null, null]` (alice & bob in `edge_pairs.ttl`).

| Behavior | Result |
|----------|--------|
| Most numeric aggregates + unbound arg | **Unbound** answer (empty) |
| One unbound row in a mixed group | **Poisons** the whole group |
| Required patterns | Incompletes **dropped**; known floats on the complete subset |
| `chi_sq` + OPTIONAL | Still returns a **number** (nulls as categories) |

Also covered on `cars.ttl` / `cities.ttl` via `EDGE_*` for pearson, covariance, percentile, median, probability.

### Agenda #2 — Tiny / empty groups

**Question:** What if there is almost no data?

| Slice (`edge_tiny.ttl`) | pearson | covariance | notes |
|-------------------------|---------|------------|--------|
| empty / single / identical / constantX | **NaN** | **0.0** | degenerate |
| empty percentile (no inputs) | — | — | **NPE** |
| empty probability | — | — | **NaN** (0/0) |
| `two` (minimal healthy line) | **1** | **1** | sanity check |

### Agenda #3 — Richer queries

**Question:** Can we use real SPARQL shapes, not only one aggregate?

On `edge_groups.ttl`:

1. **`GROUP BY ?g`** with pearson + `COUNT(*)` — A→1.0, B/C→NaN, D absent  
2. **`FILTER` that removes all rows** — pearson → NaN  
3. **Multi-aggregate SELECT** — pearson + covariance + count together  

### Agenda #4 — Dirty / weird values

**Question:** Junk types and awkward boolean / percentile inputs?

On `edge_dirty.ttl`:

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

- **JDK 25** (Temurin or equivalent)
- **Maven 3.9+**
- Network (or pre-populated local `.m2`) for dependencies

### Commands

```bash
# from repo root
mvn test

# single class
mvn test -Dtest=EdgeCaseTest

# single method
mvn test -Dtest=EdgeCaseTest#optionalAllYMissing_pearson
```

### Example Windows env (as used in local runs)

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.5.8-hotspot"
$env:Path = "$env:JAVA_HOME\bin;C:\Users\<you>\tools\apache-maven-3.9.9\bin;$env:Path"
cd path\to\more-tests
mvn test
```

---

## 11. Writing a new test

### Happy-path pattern

```java
@Test
public void testSomething() {
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
}
```

### Edge pattern (unbound)

```java
assertUnboundAggregate("edge_pairs.ttl", PREFIX + """
        SELECT (stat:pearson(?math, ?english) AS ?corr)
        WHERE {
            ?student ex:math ?math .
            OPTIONAL { ?student ex:english ?english }
        }
        """, "corr");
```

### Tips

1. Prefer a **tiny handmade TTL** when you need a specific shape (nulls, one pair, dirty string).
2. Reuse natural TTLs with an `EDGE_` name when the mess already exists (e.g. cars without weight).
3. **Lock expecteds to observed behavior** first; don’t assume “skip nulls and continue” unless the library actually does that.
4. Compute expected floats independently (Python `statistics`, spreadsheet, or hand calc on tiny sets).

---

## 12. Dependencies & local notes

| Dependency | Role |
|------------|------|
| `org.eclipse.rdf4j:*-6.2.0-M2` | RDF4J APIs + in-memory sail for tests |
| `commons-math3` | Multivariate summary stats (pearson / covariance) |
| `datasketches-java` | T-Digest for approx percentile / median |
| `com.kari3600.me:Utils` | Combinatorics helper used by chi-squared |
| JUnit 5 | Tests |

**Upstream vs this checkout**

- Upstream POM historically referenced unpublished `6.2.0-SNAPSHOT` RDF4J APIs (arity passed into factory methods).
- This checkout targets **published `6.2.0-M2`**: factories use `getMin/MaxNumberOfArguments()` and `getCollector()` without an arity argument, so `mvn test` can run without a private RDF4J build.
- `Utils` must be available in the local Maven repo (install from [Kari3600/Utils](https://github.com/Kari3600/Utils) or a stub that provides `CollectionUtil`).

These build adaptations are for **local verifiability**; keep them in mind if merging back to a SNAPSHOT-based upstream.

---

## 13. Related docs

| Doc | Contents |
|-----|----------|
| [EDGE_CASE_REPORT.md](EDGE_CASE_REPORT.md) | Full write-up of agendas #1–#4, findings tables, code map |
| This README | Project overview, how to run, datasets, how to extend tests |
| Upstream | [Kari3600/statistics-rdf4j](https://github.com/Kari3600/statistics-rdf4j) |

---

## License / authorship

Library code originates from the Kari3600 statistics-rdf4j work. Test expansions, edge fixtures, edge report, and this README are maintained in **Culturino/more-tests**.

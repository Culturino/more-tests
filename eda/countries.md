# Countries — EDA

| | |
|---|---|
| **Name** | Countries |
| **Source file** | [`countries.ttl`](../src/test/resources/countries.ttl) |
| **Type** | Natural fixture (DBpedia-style scrape) |
| **Primary class** | `dbo:Country` |
| **Used by** | Happy-path pearson / covariance |

---

## Description

Countries with development / inequality style numerics (HDI, Gini, and related fields). Small n, high completeness — stable for country-level correlation demos.

---

## How many

| | Count |
|---|------:|
| Subjects (`dbo:Country`) | **132** |
| Distinct properties seen | 15 |

---

## What kind of values

| Property | Present | Completeness | Kind (observed) |
|----------|--------:|-------------:|-----------------|
| `dbo:area` | 132 | **100.0%** | numeric |
| `dbo:giniCoefficient` | 132 | **100.0%** | numeric |
| `dbo:humanDevelopmentIndex` | 132 | **100.0%** | numeric |
| `dbo:populationDensity` | 132 | **100.0%** | numeric |
| `dbo:populationTotal` | 132 | **100.0%** | numeric |
| `dbp:areaKm` | 132 | **100.0%** | numeric |
| `dbp:gdpNominal` | 132 | **100.0%** | numeric |
| `dbp:gdpNominalPerCapita` | 132 | **100.0%** | numeric |
| `dbp:gdpPpp` | 132 | **100.0%** | numeric |
| `dbp:gdpPppPerCapita` | 132 | **100.0%** | numeric |
| `dbp:gini` | 132 | **100.0%** | numeric |
| `dbp:hdi` | 132 | **100.0%** | numeric |
| `dbp:populationDensityKm` | 132 | **100.0%** | numeric |
| `dbp:populationEstimate` | 132 | **100.0%** | numeric |
| `rdfs:label` | 132 | **100.0%** | string |

### Numeric sketch (one value per subject)

| Property | n | min | median | max | Negatives |
|----------|--:|----:|-------:|----:|----------:|
| `dbo:area` | 132 | 2.978e+08 | 2.038e+11 | 1.71e+13 | 0 |
| `dbo:giniCoefficient` | 132 | 16.6 | 35.55 | 67 | 0 |
| `dbo:humanDevelopmentIndex` | 132 | 0.388 | 0.754 | 0.97 | 0 |
| `dbo:populationDensity` | 132 | 2.239 | 91.91 | 5.437e+08 | 0 |
| `dbo:populationTotal` | 132 | 87486 | 1.224e+07 | 1.408e+09 | 0 |
| `dbp:areaKm` | 132 | 298 | 203850 | 1.71e+07 | 0 |
| `dbp:gdpNominal` | 132 | 23770 | 7.711e+10 | 1.923e+13 | 0 |
| `dbp:gdpNominalPerCapita` | 132 | 326 | 6390 | 141079 | 0 |
| `dbp:gdpPpp` | 132 | 2.97e+08 | 1.8e+11 | 4.072e+13 | 0 |
| `dbp:gdpPppPerCapita` | 132 | 5.76 | 17548 | 156755 | 0 |
| `dbp:gini` | 132 | 16.6 | 35.55 | 67 | 0 |
| `dbp:hdi` | 132 | 0.388 | 0.754 | 0.97 | 0 |
| `dbp:populationDensityKm` | 132 | 2.24 | 94.2 | 1.408e+09 | 0 |
| `dbp:populationEstimate` | 132 | 87486 | 1.224e+07 | 1.408e+09 | 0 |

---

## How complete it is

### Per property (% of 132 subjects)

| Property | Present | Missing | Completeness |
|----------|--------:|--------:|-------------:|
| `dbo:area` | 132 | 0 | **100.0%** |
| `dbo:giniCoefficient` | 132 | 0 | **100.0%** |
| `dbo:humanDevelopmentIndex` | 132 | 0 | **100.0%** |
| `dbo:populationDensity` | 132 | 0 | **100.0%** |
| `dbo:populationTotal` | 132 | 0 | **100.0%** |
| `dbp:areaKm` | 132 | 0 | **100.0%** |
| `dbp:gdpNominal` | 132 | 0 | **100.0%** |
| `dbp:gdpNominalPerCapita` | 132 | 0 | **100.0%** |
| `dbp:gdpPpp` | 132 | 0 | **100.0%** |
| `dbp:gdpPppPerCapita` | 132 | 0 | **100.0%** |
| `dbp:gini` | 132 | 0 | **100.0%** |
| `dbp:hdi` | 132 | 0 | **100.0%** |
| `dbp:populationDensityKm` | 132 | 0 | **100.0%** |
| `dbp:populationEstimate` | 132 | 0 | **100.0%** |
| `rdfs:label` | 132 | 0 | **100.0%** |

### Main bivariate pair: `dbo:humanDevelopmentIndex` × `dbo:giniCoefficient`

| Bucket | Count | Meaning |
|--------|------:|---------|
| **both** | 132 | Has both → survives a REQUIRED join |
| **left_only** | 0 | Has `dbo:humanDevelopmentIndex`, missing `dbo:giniCoefficient` |
| **right_only** | 0 | Has `dbo:giniCoefficient`, missing `dbo:humanDevelopmentIndex` |
| **neither** | 0 | Has type but neither property |

**Pair completeness: 100.0%** (132 / 132).

Under REQUIRED patterns, effective n ≈ **132**. Under `OPTIONAL`, incompletes can enter as blanks → many numeric aggregates go **unbound**.

### Pair: `dbp:hdi` × `dbp:gini`

| Bucket | Count | Meaning |
|--------|------:|---------|
| **both** | 132 | Has both → survives a REQUIRED join |
| **left_only** | 0 | Has `dbp:hdi`, missing `dbp:gini` |
| **right_only** | 0 | Has `dbp:gini`, missing `dbp:hdi` |
| **neither** | 0 | Has type but neither property |

**Pair completeness: 100.0%** (132 / 132).

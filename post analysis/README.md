# Post analysis — line-by-line walkthrough of the new tests

Plain-language notes from walking the **edge-case test suite** (explained simply).

These docs are for humans. They do not run tests. The real code lives under `src/test/java/...`.

## Reading order

| # | Doc | Covers |
|---|-----|--------|
| 0 | [00-big-picture.md](00-big-picture.md) | What this project is; folder map |
| 1 | [01-TestUtil.md](01-TestUtil.md) | Shared remote control for all tests |
| 2 | [02-PearsonEdgeTest.md](02-PearsonEdgeTest.md) | Full walkthrough of every pearson edge test |
| 3 | [03-CovarianceEdgeTest.md](03-CovarianceEdgeTest.md) | Full walkthrough of every covariance edge test |
| 4 | [04-PercentileEdgeTest.md](04-PercentileEdgeTest.md) | Percentile + approx_median edge tests |
| 5 | [05-ProbabilityEdgeTest.md](05-ProbabilityEdgeTest.md) | `stat:p` / conditional probability edge tests |
| 6 | [06-ChiSquaredEdgeTest.md](06-ChiSquaredEdgeTest.md) | `stat:chi_sq` edge tests (the odd one out) |
| — | [99-progress.md](99-progress.md) | What’s done vs still to walk |

## Related project docs

- [README.md](../README.md) — project overview
- [EDGE_CASE_REPORT.md](../EDGE_CASE_REPORT.md) — edge-case findings
- [src/test/resources/eda/](../src/test/resources/eda/) — per-dataset EDA
- Table views of handmade data: [custom-data/](../custom-data/)

## Code under discussion

| Class | Path |
|-------|------|
| `TestUtil` | [`../src/test/java/com/kari3600/me/statistics_rdf4j/TestUtil.java`](../src/test/java/com/kari3600/me/statistics_rdf4j/TestUtil.java) |
| `PearsonEdgeTest` | [`../src/test/java/com/kari3600/me/statistics_rdf4j/PearsonEdgeTest.java`](../src/test/java/com/kari3600/me/statistics_rdf4j/PearsonEdgeTest.java) |
| `CovarianceEdgeTest` | [`../src/test/java/com/kari3600/me/statistics_rdf4j/CovarianceEdgeTest.java`](../src/test/java/com/kari3600/me/statistics_rdf4j/CovarianceEdgeTest.java) |
| `PercentileEdgeTest` | [`../src/test/java/com/kari3600/me/statistics_rdf4j/PercentileEdgeTest.java`](../src/test/java/com/kari3600/me/statistics_rdf4j/PercentileEdgeTest.java) |
| `ProbabilityEdgeTest` | [`../src/test/java/com/kari3600/me/statistics_rdf4j/ProbabilityEdgeTest.java`](../src/test/java/com/kari3600/me/statistics_rdf4j/ProbabilityEdgeTest.java) |
| `ChiSquaredEdgeTest` | [`../src/test/java/com/kari3600/me/statistics_rdf4j/ChiSquaredEdgeTest.java`](../src/test/java/com/kari3600/me/statistics_rdf4j/ChiSquaredEdgeTest.java) |

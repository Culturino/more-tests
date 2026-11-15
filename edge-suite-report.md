# Edge suite report — statistics-rdf4j

**Repo:** more-tests · **Branch:** develop  
**Scope:** SPARQL statistics aggregates on RDF4J — edge findings + correctness verdict  
**Suite status:** green (`mvn test`, `mvn test "-Dtest=*EdgeTest"`) · ~52 edge tests · 5 `*EdgeTest` classes

---

## Overall verdict

**Mixed.**

- OPTIONAL / type-error → unbound matches normal SPARQL behavior.
- Degenerate pearson → NaN is fine stats behavior.
- **Clear bugs (1):** empty percentile **NPE**. (Raw `xsd:boolean` in `stat:p` fixed upstream `8d15da0`.)
- Several other cases are **debatable design**, not crashes.

---

## What was tested

| Agenda | Fixture | Question |
|--------|---------|----------|
| #1 Incomplete pairs | `pairs`, `chi` (+ cars/cities) | OPTIONAL missing into aggregates? |
| #2 Tiny groups | `tiny` | Empty / n=1 / no variance? |
| #3 Richer SPARQL | `groups` | GROUP BY / empty FILTER / multi-agg? |
| #4 Dirty values | `dirty` | Wrong types, booleans, bad ranks? |

**Test classes:** `PearsonEdgeTest` (~16) · `CovarianceEdgeTest` (~12) · `PercentileEdgeTest` (~11) · `ProbabilityEdgeTest` (~10) · `ChiSquaredEdgeTest` (~3)

**Fixtures:** `pairs` · `chi` · `tiny` · `groups` · `dirty` · plus cars/cities controls  
**Related docs:** `testing2.md` · `post analysis/` · `eda/`

---

## Observed behavior vs expectation

| Behavior | Observed | Verdict |
|----------|----------|---------|
| OPTIONAL unbound / bad type | Unbound; one bad row poisons group | Expected (SPARQL) |
| Non-numeric junk in numeric agg | Unbound | Expected |
| `chi_sq` + OPTIONAL missing | Still a number (null as category) | Debatable / inconsistent |
| Degenerate pearson | NaN | Expected (stats) |
| Degenerate / empty covariance | 0.0 (incl. empty & n=1) | OK for identical; empty/n=1 weak |
| Empty percentile | NullPointerException | **Bug** |
| Empty probability | NaN (0/0) | Debatable |
| `stat:p(raw xsd:boolean)` | **1.0** on all-true (Kari `8d15da0` fix) | Fixed upstream |
| Percentile rank 1.5 | Throws (hard crash) | Debatable (prefer unbound) |

---

## Agenda findings (short)

### #1 Incomplete / null pairs
REQUIRED joins drop incompletes. OPTIONAL feeds unbound into the aggregate → most stats return unbound (poison). **`chi_sq` is the odd one out:** missing values become contingency levels and a number still comes back.

### #2 Tiny / empty groups
Pearson → NaN when variance/n is insufficient. Covariance → 0 on degenerate Commons-Math paths. Empty `p` → NaN. **Empty percentile never sets the rank Double → NPE in `getQuantile`.**

### #3 Richer queries
GROUP BY, empty FILTER, and multi-aggregate SELECT behave as coded: healthy group A → 1.0; B/C → NaN; D absent under REQUIRED. Covered mainly in `PearsonEdgeTest`.

### #4 Dirty / weird values
Strings in numeric paths → unbound. Raw `xsd:boolean` in `stat:p(?flag)` works (Kari fix). Rank outside `[0,1]` throws instead of a held type error → unbound.

---

## Fix priority

| Priority | Item | Why |
|----------|------|-----|
| **P0** | Empty percentile NPE | Should be unbound / clean type error, not crash |
| ~~**P0**~~ | ~~Accept `xsd:boolean` in `asBoolean` / `p()`~~ | **Done** in Kari `8d15da0` — pulled into this fork |
| **P1** | Empty / n=1 covariance → NaN or unbound | `0.0` looks like a real answer for undefined cov |
| **P1** | Empty `p` / bad percentile rank → unbound | Align with SPARQL error → unbound policy |
| **P2** | Document or align `chi_sq` null policy | Keep null-as-category only if intentional |

---

## Notes

- Source: edge `*EdgeTest` suite + main collectors (`ApproximatePercentileCollector`, `CollectorUtil.asBoolean`, `ChiSquaredCollector`, `ProbabilityCollector`, `CovarianceCollector`).
- Tests **document** current behavior; they do not change library policy.
- Narrative twin: `testing2.md`.

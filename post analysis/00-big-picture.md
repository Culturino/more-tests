# 0 — Big picture

## What this project is (30 seconds)

This project teaches a database that stores **graphs** (not Excel tables) how to do **stats** — correlation, covariance, percentile, probability, chi-squared, etc.

Think: you have a pile of facts like “car A weighs 1400”. You write a question in a language called **SPARQL**. The Java code in this repo is the calculator that answers those questions.

There are two kinds of files:

1. **The calculator** (`src/main`) — the real product  
2. **Practice exams** (`src/test`) — fake datasets + tests that check the calculator isn’t broken  

## Map of the desk

```text
more-tests/
├── pom.xml                 ← shopping list for the build tool (Maven)
├── README.md               ← human guide
├── EDGE_CASE_REPORT.md     ← “weird situations” write-up
├── custom-data/            ← tables so humans can read small fake datasets
├── post analysis/          ← this walkthrough (you are here)
├── src/test/resources/eda/ ← per-dataset EDA write-ups
└── src/
    ├── main/               ← the calculator (Java)
    └── test/               ← exams + answer sheets
```

Ignore `target/` — leftovers from running tests.

## Root docs (quick)

| File | Role |
|------|------|
| `pom.xml` | Maven config: Java 25, RDF4J, JUnit, math libs. Recipe so `mvn test` works. |
| `README.md` | Tour guide. Doesn’t run anything. |
| `EDGE_CASE_REPORT.md` | Diary of weird cases (missing scores, tiny groups, garbage types). |
| `src/test/resources/eda/` | Per-dataset EDA (counts, completeness, value kinds). |

## `custom-data/` (human tables)

Small handmade datasets as markdown tables. The **real** files tests load are the twin `.ttl` files under `src/test/resources/custom-data/`.

| File | Meaning |
|------|---------|
| `pairs.md` | students, some missing English scores |
| `chi.md` | gender × order, some blanks |
| `tiny.md` | tiny groups (0, 1, 2 points) |
| `groups.md` | groups A–D for `GROUP BY` |
| `dirty.md` | dirty / wrong-type values |

`.md` = for your eyes. `.ttl` = for the computer.

## How every edge test works

1. Load a tiny (or natural) database from a `.ttl` file  
2. Run a SPARQL question  
3. Check the answer  

`TestUtil` is the shared remote control. Edge test classes only write the **questions** and the **expected answers**.

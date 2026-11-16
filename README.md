# more-tests (statistics-rdf4j) — code

SPARQL statistics aggregates for Eclipse RDF4J, with natural DBpedia-style fixtures and handmade edge-case datasets.

| | |
|---|---|
| **GitHub** | https://github.com/Culturino/more-tests |
| **Branch** | `code` |
| **Upstream** | https://github.com/Kari3600/statistics-rdf4j |

## Layout

```text
more-tests/
├── pom.xml
├── custom-data/              ← markdown table views of handmade edge fixtures
└── src/
    ├── main/                 ← aggregate implementations
    └── test/
        ├── java/             ← happy-path + edge tests
        └── resources/        ← TTL datasets (natural + custom-data/)
```

Reports / EDA write-ups live on the `reports` branch.

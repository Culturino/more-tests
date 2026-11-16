# 1 — `TestUtil.java` (the remote control)

Source: [`src/test/java/com/kari3600/me/statistics_rdf4j/TestUtil.java`](../../src/test/java/com/kari3600/me/statistics_rdf4j/TestUtil.java)

All edge tests do the same dance. `TestUtil` is the shared remote control for that dance.

---

## Header / imports

- `package com.kari3600.me.statistics_rdf4j;` → this file lives in this folder-of-code.
- `import ...` → borrow tools from RDF4J (graph DB) and JUnit (test asserts).
- `assertEquals` / `assertFalse` / `assertNull` → “fail the test if this isn’t true.”

---

## `query(dataset, queryString, resultConsumer)`

Load data + run SPARQL.

| Piece | Plain English |
|-------|----------------|
| `public static void query(...)` | Anyone can call this; returns nothing; you pass a callback for results. |
| `String dataset` | Filename like `"cars.ttl"`. |
| `String queryString` | The SPARQL text. |
| `Consumer<TupleQueryResult> resultConsumer` | “Here’s a function: do something with the answer rows.” |
| `new MemoryStore()` | Empty DB that lives only in RAM (disappears when done). |
| `new SailRepository(...)` | Wrap that store so we can run queries. |
| `db.getConnection()` | Open a session to talk to the DB. |
| `getResourceAsStream("/" + dataset)` | Open the `.ttl` file from test resources. |
| `if (input == null) throw ...` | File missing → crash with a clear message. |
| `conn.add(..., RDFFormat.TURTLE)` | Pour the Turtle facts into the DB. |
| `PREFIX stat: <...>` glued on | Auto-add the stats namespace so you can write `stat:pearson`. |
| `prepareTupleQuery` | Compile the SPARQL string. |
| `query.evaluate()` | Run it. |
| `resultConsumer.accept(result)` | Hand the rows to the caller. |
| `db.shutDown()` | Throw away the temporary DB. |

---

## `query1Group1Var(dataset, query, variable)`

“I expect exactly one number named `?corr` (or whatever).”

- Grab first row → get that variable → cast to a number-literal → return it.
- Used when the happy answer is a real float like `1.0`.

---

## `queryAll(dataset, query)`

“Give me every row as a list.”

- `BindingSet` ≈ one spreadsheet row of `?name → value`.
- Used when we need to inspect unbound / multi-row results.

---

## `query1VarOrNull(dataset, query, variable)`

Value of a variable in the single result row, or `null` if unbound / no rows.

---

## `assertUnboundAggregate(dataset, query, variable)`

The big edge-case checker:

1. Run the query, collect rows.
2. There must be **exactly 1** answer row (aggregate still “returns a row”).
3. That row must **not** have a binding for `?corr` / `?cov` / etc.
4. Getting the value must be `null`.

Meaning: the calculator didn’t explode the whole query, but it also **didn’t produce a number** — the answer cell is blank. That’s “poisoned / unbound.”

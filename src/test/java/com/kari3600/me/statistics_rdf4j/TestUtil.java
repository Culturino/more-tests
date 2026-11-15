package com.kari3600.me.statistics_rdf4j;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class TestUtil {
    public static void query(String dataset, String queryString, Consumer<TupleQueryResult> resultConsumer) {
        Repository db = new SailRepository(new MemoryStore());

        try (RepositoryConnection conn = db.getConnection()) {
            try (InputStream input = TestUtil.class.getResourceAsStream("/" + dataset)) {
                if (input == null) {
                    throw new RuntimeException(dataset + " not found");
                }
                conn.add(input, "", RDFFormat.TURTLE);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load dataset: "+e);
            }

            queryString = "PREFIX stat: <http://me.kari3600.com/statistics-rdf4j#>\n"+queryString;

            TupleQuery query = conn.prepareTupleQuery(queryString);

            try (TupleQueryResult result = query.evaluate()) {
                resultConsumer.accept(result);
            }
        } finally {
            db.shutDown();
        }
    }

    public static Literal query1Group1Var(String dataset, String queryString, String variable) {
        AtomicReference<Literal> result = new AtomicReference<>();
        query(dataset, queryString, rs -> result.set((Literal) rs.next().getValue(variable)));
        return result.get();
    }

    /** Run query and collect all binding sets (for multi-row / empty checks). */
    public static List<BindingSet> queryAll(String dataset, String queryString) {
        List<BindingSet> rows = new ArrayList<>();
        query(dataset, queryString, rs -> {
            while (rs.hasNext()) {
                rows.add(rs.next());
            }
        });
        return rows;
    }

    /** Value of a variable in the single result row, or null if unbound / no rows. */
    public static Value query1VarOrNull(String dataset, String queryString, String variable) {
        List<BindingSet> rows = queryAll(dataset, queryString);
        if (rows.isEmpty()) {
            return null;
        }
        return rows.get(0).getValue(variable);
    }
}

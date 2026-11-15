package com.kari3600.me.statistics_rdf4j;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.kari3600.me.statistics_rdf4j.TestUtil.assertUnboundAggregate;
import static com.kari3600.me.statistics_rdf4j.TestUtil.query1Group1Var;
import static com.kari3600.me.statistics_rdf4j.TestUtil.queryAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Edge cases for {@code stat:pearson}.
 * Fixtures: custom-data/pairs, tiny, groups, dirty; cars/cities.
 */
public class PearsonEdgeTest {

    private static final String PREFIX = """
            PREFIX ex: <http://example.org/edge#>
            """;

    private static final String DBPEDIA = """
            PREFIX dbo: <http://dbpedia.org/ontology/>
            PREFIX dbp: <http://dbpedia.org/property/>
            """;

    private static void assertTinyPearson(String caseName) {
        double d = query1Group1Var("custom-data/tiny.ttl", PREFIX + """
                SELECT (stat:pearson(?x, ?y) AS ?corr)
                WHERE {
                    ?s ex:case "%s" ;
                       ex:x ?x ;
                       ex:y ?y .
                }
                """.formatted(caseName), "corr").doubleValue();
        assertTrue(Double.isNaN(d), caseName + " pearson expected NaN, got " + d);
    }

    // --- pairs: healthy controls ---

    @Test
    public void requiredPerfectLine_pearson() {
        assertEquals(1.0, query1Group1Var("custom-data/pairs.ttl", PREFIX + """
                SELECT (stat:pearson(?math, ?english) AS ?corr)
                WHERE {
                    VALUES ?student { ex:cara ex:dan ex:ella ex:finn }
                    ?student ex:math ?math ;
                             ex:english ?english .
                }
                """, "corr").doubleValue(), 1e-9);
    }

    @Test
    public void requiredAllComplete_pearson() {
        assertEquals(0.45191786568701003, query1Group1Var("custom-data/pairs.ttl", PREFIX + """
                SELECT (stat:pearson(?math, ?english) AS ?corr)
                WHERE {
                    ?student a ex:Student ;
                        ex:math ?math ;
                        ex:english ?english .
                }
                """, "corr").doubleValue(), 1e-6);
    }

    // --- pairs: edge cases ---

    @Test
    public void optionalAllYMissing_pearson() {
        assertUnboundAggregate("custom-data/pairs.ttl", PREFIX + """
                SELECT (stat:pearson(?math, ?english) AS ?corr)
                WHERE {
                    VALUES ?student { ex:alice ex:bob }
                    ?student ex:math ?math .
                    OPTIONAL { ?student ex:english ?english }
                }
                """, "corr");
    }

    @Test
    public void optionalMixed_pearson() {
        assertUnboundAggregate("custom-data/pairs.ttl", PREFIX + """
                SELECT (stat:pearson(?math, ?english) AS ?corr)
                WHERE {
                    ?student a ex:Student ;
                        ex:math ?math .
                    OPTIONAL { ?student ex:english ?english }
                }
                """, "corr");
    }

    @Test
    public void requiredIncompleteOnly_pearson() {
        List<BindingSet> rows = queryAll("custom-data/pairs.ttl", PREFIX + """
                SELECT (stat:pearson(?math, ?english) AS ?corr)
                WHERE {
                    VALUES ?student { ex:alice ex:bob }
                    ?student ex:math ?math ;
                             ex:english ?english .
                }
                """);
        assertEquals(1, rows.size());
        Value v = rows.get(0).getValue("corr");
        if (v == null) {
            assertFalse(rows.get(0).hasBinding("corr"));
        } else {
            assertTrue(v instanceof Literal);
            double d = ((Literal) v).doubleValue();
            assertTrue(Double.isNaN(d) || Double.isInfinite(d),
                    "expected NaN/Inf for empty pair set, got " + d);
        }
    }

    // --- cars / cities ---

    @Test
    public void carsOptionalMissingWeight_pearson() {
        assertUnboundAggregate("cars.ttl", DBPEDIA + """
                SELECT (stat:pearson(?length, ?weight) AS ?corr)
                WHERE {
                    ?car a dbo:Automobile ;
                        dbp:length ?length .
                    OPTIONAL { ?car dbp:weight ?weight }
                }
                """, "corr");
    }

    @Test
    public void citiesOptionalMissingElevation_pearson() {
        assertUnboundAggregate("cities.ttl", DBPEDIA + """
                SELECT (stat:pearson(?population, ?elevation) AS ?corr)
                WHERE {
                    ?city a dbo:City ;
                        dbo:populationTotal ?population .
                    OPTIONAL { ?city dbo:elevation ?elevation }
                }
                """, "corr");
    }

    // --- tiny: healthy control then edges ---

    @Test
    public void tinyTwo_pearson() {
        assertEquals(1.0, query1Group1Var("custom-data/tiny.ttl", PREFIX + """
                SELECT (stat:pearson(?x, ?y) AS ?corr)
                WHERE {
                    ?s ex:case "two" ; ex:x ?x ; ex:y ?y .
                }
                """, "corr").doubleValue(), 1e-9);
    }

    @Test
    public void tinyEmpty_pearson() {
        assertTinyPearson("empty");
    }

    @Test
    public void tinySingle_pearson() {
        assertTinyPearson("single");
    }

    @Test
    public void tinyIdentical_pearson() {
        assertTinyPearson("identical");
    }

    @Test
    public void tinyConstantX_pearson() {
        assertTinyPearson("constantX");
    }

    // --- groups: healthy control then edges ---

    @Test
    public void richerMultiAggregate_pearson() {
        List<BindingSet> rows = queryAll("custom-data/groups.ttl", PREFIX + """
                SELECT (stat:pearson(?math, ?english) AS ?corr)
                       (stat:covariance(?math, ?english) AS ?cov)
                       (COUNT(*) AS ?n)
                WHERE {
                    ?s ex:group "A" ;
                       ex:math ?math ;
                       ex:english ?english .
                }
                """);
        assertEquals(1, rows.size());
        BindingSet r = rows.get(0);
        assertEquals(1.0, ((Literal) r.getValue("corr")).doubleValue(), 1e-9);
        assertEquals(3.333333333333333, ((Literal) r.getValue("cov")).doubleValue(), 1e-9);
        assertEquals(4, ((Literal) r.getValue("n")).intValue());
    }

    @Test
    public void richerGroupBy_pearson() {
        List<BindingSet> rows = queryAll("custom-data/groups.ttl", PREFIX + """
                SELECT ?g (stat:pearson(?math, ?english) AS ?corr) (COUNT(*) AS ?n)
                WHERE {
                    ?s ex:group ?g ;
                       ex:math ?math ;
                       ex:english ?english .
                }
                GROUP BY ?g
                ORDER BY ?g
                """);
        assertEquals(3, rows.size());

        BindingSet a = rows.get(0);
        assertEquals("A", a.getValue("g").stringValue());
        assertEquals(1.0, ((Literal) a.getValue("corr")).doubleValue(), 1e-9);
        assertEquals(4, ((Literal) a.getValue("n")).intValue());

        BindingSet b = rows.get(1);
        assertEquals("B", b.getValue("g").stringValue());
        assertTrue(Double.isNaN(((Literal) b.getValue("corr")).doubleValue()));
        assertEquals(1, ((Literal) b.getValue("n")).intValue());

        BindingSet c = rows.get(2);
        assertEquals("C", c.getValue("g").stringValue());
        assertTrue(Double.isNaN(((Literal) c.getValue("corr")).doubleValue()));
        assertEquals(2, ((Literal) c.getValue("n")).intValue());
    }

    @Test
    public void richerFilterEmpties_pearson() {
        double d = query1Group1Var("custom-data/groups.ttl", PREFIX + """
                SELECT (stat:pearson(?math, ?english) AS ?corr)
                WHERE {
                    ?s ex:group "A" ;
                       ex:math ?math ;
                       ex:english ?english .
                    FILTER(?math > 1000)
                }
                """, "corr").doubleValue();
        assertTrue(Double.isNaN(d), "expected NaN when FILTER removes all rows, got " + d);
    }

    // --- dirty ---

    @Test
    public void dirtyMixedNonNumeric_pearson() {
        assertUnboundAggregate("custom-data/dirty.ttl", PREFIX + """
                SELECT (stat:pearson(?val, ?other) AS ?corr)
                WHERE {
                    ?s ex:case "mixed" ; ex:val ?val ; ex:other ?other .
                }
                """, "corr");
    }
}

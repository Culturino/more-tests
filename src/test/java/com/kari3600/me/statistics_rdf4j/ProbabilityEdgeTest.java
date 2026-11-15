package com.kari3600.me.statistics_rdf4j;

import org.junit.jupiter.api.Test;

import static com.kari3600.me.statistics_rdf4j.TestUtil.assertUnboundAggregate;
import static com.kari3600.me.statistics_rdf4j.TestUtil.query1Group1Var;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Edge cases for {@code stat:p} (probability / conditional probability).
 * Fixtures: custom-data/edge_pairs, edge_tiny, edge_dirty; cars/cities.
 */
public class ProbabilityEdgeTest {

    private static final String PREFIX = """
            PREFIX ex: <http://example.org/edge#>
            """;

    private static final String DBPEDIA = """
            PREFIX dbo: <http://dbpedia.org/ontology/>
            PREFIX dbp: <http://dbpedia.org/property/>
            """;

    @Test
    public void EDGE_carsOptionalMissingWeight_probability() {
        assertUnboundAggregate("cars.ttl", DBPEDIA + """
                SELECT (stat:p(?weight > 1000) AS ?prob)
                WHERE {
                    ?car a dbo:Automobile .
                    OPTIONAL { ?car dbp:weight ?weight }
                }
                """, "prob");
    }

    @Test
    public void EDGE_carsOptionalMissingWeight_conditionalProbability() {
        assertUnboundAggregate("cars.ttl", DBPEDIA + """
                SELECT (stat:p(?weight > 1000, ?length > 100) AS ?prob)
                WHERE {
                    ?car a dbo:Automobile .
                    OPTIONAL { ?car dbp:weight ?weight }
                    OPTIONAL { ?car dbp:length ?length }
                }
                """, "prob");
    }

    @Test
    public void EDGE_citiesOptionalMissingElevation_probability() {
        assertUnboundAggregate("cities.ttl", DBPEDIA + """
                SELECT (stat:p(?elevation > 1000) AS ?prob)
                WHERE {
                    ?city a dbo:City .
                    OPTIONAL { ?city dbo:elevation ?elevation }
                }
                """, "prob");
    }

    @Test
    public void EDGE_edgePairsOptionalEnglish_probability() {
        assertUnboundAggregate("custom-data/edge_pairs.ttl", PREFIX + """
                SELECT (stat:p(?english > 5) AS ?prob)
                WHERE {
                    ?student a ex:Student .
                    OPTIONAL { ?student ex:english ?english }
                }
                """, "prob");
    }

    @Test
    public void tinyEmpty_probabilityNaN() {
        double d = query1Group1Var("custom-data/edge_tiny.ttl", PREFIX + """
                SELECT (stat:p(?x > 0) AS ?prob)
                WHERE {
                    VALUES ?s { ex:empty2 ex:empty3 }
                    ?s ex:x ?x .
                }
                """, "prob").doubleValue();
        assertTrue(Double.isNaN(d), "expected NaN for empty probability, got " + d);
    }

    @Test
    public void dirtyRawBooleanFlag_probabilityUnbound() {
        assertUnboundAggregate("custom-data/edge_dirty.ttl", PREFIX + """
                SELECT (stat:p(?flag) AS ?prob)
                WHERE {
                    ?s ex:case "allTrue" ; ex:flag ?flag .
                }
                """, "prob");
    }

    @Test
    public void dirtyAllTrue_probabilityOne() {
        assertEquals(1.0, query1Group1Var("custom-data/edge_dirty.ttl", PREFIX + """
                SELECT (stat:p(?flag = true) AS ?prob)
                WHERE {
                    ?s ex:case "allTrue" ; ex:flag ?flag .
                }
                """, "prob").doubleValue(), 1e-9);
    }

    @Test
    public void dirtyAllFalse_probabilityZero() {
        assertEquals(0.0, query1Group1Var("custom-data/edge_dirty.ttl", PREFIX + """
                SELECT (stat:p(?flag = true) AS ?prob)
                WHERE {
                    ?s ex:case "allFalse" ; ex:flag ?flag .
                }
                """, "prob").doubleValue(), 1e-9);
    }

    @Test
    public void dirtyEmptyCondition_conditionalProbabilityNaN() {
        double d = query1Group1Var("custom-data/edge_dirty.ttl", PREFIX + """
                SELECT (stat:p(?val > 15, ?flag = true) AS ?prob)
                WHERE {
                    ?s ex:case "emptyCond" ; ex:val ?val ; ex:flag ?flag .
                }
                """, "prob").doubleValue();
        assertTrue(Double.isNaN(d), "expected NaN when condition never matches, got " + d);
    }

    @Test
    public void dirtyClean_probabilityBaseline() {
        assertEquals(2.0 / 3.0, query1Group1Var("custom-data/edge_dirty.ttl", PREFIX + """
                SELECT (stat:p(?val > 15) AS ?prob)
                WHERE {
                    ?s ex:case "clean" ; ex:val ?val .
                }
                """, "prob").doubleValue(), 1e-9);
    }
}

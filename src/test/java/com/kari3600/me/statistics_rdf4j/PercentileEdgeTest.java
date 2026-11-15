package com.kari3600.me.statistics_rdf4j;

import org.junit.jupiter.api.Test;

import static com.kari3600.me.statistics_rdf4j.TestUtil.assertUnboundAggregate;
import static com.kari3600.me.statistics_rdf4j.TestUtil.query1Group1Var;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Edge cases for {@code stat:percentile} and {@code stat:approx_median}.
 * Fixtures: custom-data/edge_pairs, edge_tiny, edge_dirty; cars/cities.
 */
public class PercentileEdgeTest {

    private static final String PREFIX = """
            PREFIX ex: <http://example.org/edge#>
            """;

    private static final String DBPEDIA = """
            PREFIX dbo: <http://dbpedia.org/ontology/>
            PREFIX dbp: <http://dbpedia.org/property/>
            """;

    @Test
    public void EDGE_carsOptionalMissingWeight_percentile() {
        assertUnboundAggregate("cars.ttl", DBPEDIA + """
                SELECT (stat:percentile(?weight, 0.5) AS ?p)
                WHERE {
                    ?car a dbo:Automobile .
                    OPTIONAL { ?car dbp:weight ?weight }
                }
                """, "p");
    }

    @Test
    public void EDGE_carsOptionalMissingWeight_approxMedian() {
        assertUnboundAggregate("cars.ttl", DBPEDIA + """
                SELECT (stat:approx_median(?weight) AS ?med)
                WHERE {
                    ?car a dbo:Automobile .
                    OPTIONAL { ?car dbp:weight ?weight }
                }
                """, "med");
    }

    @Test
    public void EDGE_citiesOptionalMissingElevation_percentile() {
        assertUnboundAggregate("cities.ttl", DBPEDIA + """
                SELECT (stat:percentile(?elevation, 0.5) AS ?p)
                WHERE {
                    ?city a dbo:City .
                    OPTIONAL { ?city dbo:elevation ?elevation }
                }
                """, "p");
    }

    @Test
    public void EDGE_edgePairsOptionalEnglish_percentile() {
        assertUnboundAggregate("custom-data/edge_pairs.ttl", PREFIX + """
                SELECT (stat:percentile(?english, 0.5) AS ?p)
                WHERE {
                    ?student a ex:Student .
                    OPTIONAL { ?student ex:english ?english }
                }
                """, "p");
    }

    @Test
    public void EDGE_edgePairsOptionalEnglish_approxMedian() {
        assertUnboundAggregate("custom-data/edge_pairs.ttl", PREFIX + """
                SELECT (stat:approx_median(?english) AS ?med)
                WHERE {
                    ?student a ex:Student .
                    OPTIONAL { ?student ex:english ?english }
                }
                """, "med");
    }

    @Test
    public void tinyEmpty_percentileNpe() {
        assertThrows(NullPointerException.class, () ->
                query1Group1Var("custom-data/edge_tiny.ttl", PREFIX + """
                        SELECT (stat:percentile(?x, 0.5) AS ?p)
                        WHERE {
                            VALUES ?s { ex:empty2 ex:empty3 }
                            ?s ex:x ?x .
                        }
                        """, "p"));
    }

    @Test
    public void tinySingle_percentileIsTheValue() {
        assertEquals(3.0, query1Group1Var("custom-data/edge_tiny.ttl", PREFIX + """
                SELECT (stat:percentile(?x, 0.5) AS ?p)
                WHERE {
                    ?s ex:case "single" ; ex:x ?x .
                }
                """, "p").doubleValue(), 1e-9);
    }

    @Test
    public void tinyIdentical_percentile() {
        assertEquals(5.0, query1Group1Var("custom-data/edge_tiny.ttl", PREFIX + """
                SELECT (stat:percentile(?x, 0.5) AS ?p)
                WHERE {
                    ?s ex:case "identical" ; ex:x ?x .
                }
                """, "p").doubleValue(), 1e-6);
    }

    @Test
    public void dirtyStringNumber_percentileUnbound() {
        assertUnboundAggregate("custom-data/edge_dirty.ttl", PREFIX + """
                SELECT (stat:percentile(?val, 0.5) AS ?p)
                WHERE {
                    ?s ex:case "stringNum" ; ex:val ?val .
                }
                """, "p");
    }

    @Test
    public void dirtyDuplicateValues_percentile() {
        assertEquals(2.0, query1Group1Var("custom-data/edge_dirty.ttl", PREFIX + """
                SELECT (stat:percentile(?val, 0.5) AS ?p)
                WHERE {
                    ?s ex:case "dup" ; ex:val ?val .
                }
                """, "p").doubleValue(), 1e-6);
    }

    @Test
    public void dirtyPercentileOutOfRange_throws() {
        assertThrows(RuntimeException.class, () ->
                query1Group1Var("custom-data/edge_dirty.ttl", PREFIX + """
                        SELECT (stat:percentile(?val, 1.5) AS ?p)
                        WHERE {
                            ?s ex:case "clean" ; ex:val ?val .
                        }
                        """, "p"));
    }
}

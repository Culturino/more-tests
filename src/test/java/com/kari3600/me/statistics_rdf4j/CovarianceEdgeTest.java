package com.kari3600.me.statistics_rdf4j;

import org.junit.jupiter.api.Test;

import static com.kari3600.me.statistics_rdf4j.TestUtil.assertUnboundAggregate;
import static com.kari3600.me.statistics_rdf4j.TestUtil.query1Group1Var;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Edge cases for {@code stat:covariance}.
 * Fixtures: custom-data/edge_pairs, edge_tiny; cars/cities.
 */
public class CovarianceEdgeTest {

    private static final String PREFIX = """
            PREFIX ex: <http://example.org/edge#>
            """;

    private static final String DBPEDIA = """
            PREFIX dbo: <http://dbpedia.org/ontology/>
            PREFIX dbp: <http://dbpedia.org/property/>
            """;

    private static void assertTinyCovarianceZero(String caseName) {
        assertEquals(0.0, query1Group1Var("custom-data/edge_tiny.ttl", PREFIX + """
                SELECT (stat:covariance(?x, ?y) AS ?cov)
                WHERE {
                    ?s ex:case "%s" ;
                       ex:x ?x ;
                       ex:y ?y .
                }
                """.formatted(caseName), "cov").doubleValue(), 1e-12);
    }

    @Test
    public void optionalAllYMissing_covariance() {
        assertUnboundAggregate("custom-data/edge_pairs.ttl", PREFIX + """
                SELECT (stat:covariance(?math, ?english) AS ?cov)
                WHERE {
                    VALUES ?student { ex:alice ex:bob }
                    ?student ex:math ?math .
                    OPTIONAL { ?student ex:english ?english }
                }
                """, "cov");
    }

    @Test
    public void optionalMixed_covariancePoisonsOnUnbound() {
        assertUnboundAggregate("custom-data/edge_pairs.ttl", PREFIX + """
                SELECT (stat:covariance(?math, ?english) AS ?cov)
                WHERE {
                    ?student a ex:Student ;
                        ex:math ?math .
                    OPTIONAL { ?student ex:english ?english }
                }
                """, "cov");
    }

    @Test
    public void requiredPerfectLine_covariance() {
        assertEquals(25.833333333333332, query1Group1Var("custom-data/edge_pairs.ttl", PREFIX + """
                SELECT (stat:covariance(?math, ?english) AS ?cov)
                WHERE {
                    VALUES ?student { ex:cara ex:dan ex:ella ex:finn }
                    ?student ex:math ?math ;
                             ex:english ?english .
                }
                """, "cov").doubleValue(), 1e-6);
    }

    @Test
    public void requiredAllComplete_covariance() {
        assertEquals(9.933333333333334, query1Group1Var("custom-data/edge_pairs.ttl", PREFIX + """
                SELECT (stat:covariance(?math, ?english) AS ?cov)
                WHERE {
                    ?student a ex:Student ;
                        ex:math ?math ;
                        ex:english ?english .
                }
                """, "cov").doubleValue(), 1e-6);
    }

    @Test
    public void EDGE_carsOptionalMissingWeight_covariance() {
        assertUnboundAggregate("cars.ttl", DBPEDIA + """
                SELECT (stat:covariance(?length, ?weight) AS ?cov)
                WHERE {
                    ?car a dbo:Automobile ;
                        dbp:length ?length .
                    OPTIONAL { ?car dbp:weight ?weight }
                }
                """, "cov");
    }

    @Test
    public void EDGE_carsRequiredComplete_covarianceStillWorks() {
        assertEquals(21770421.170564972, query1Group1Var("cars.ttl", DBPEDIA + """
                SELECT (stat:covariance(?length, ?weight) AS ?cov)
                WHERE {
                    ?car a dbo:Automobile ;
                        dbp:length ?length ;
                        dbp:weight ?weight .
                }
                """, "cov").doubleValue(), 1.0);
    }

    @Test
    public void EDGE_citiesOptionalMissingElevation_covariance() {
        assertUnboundAggregate("cities.ttl", DBPEDIA + """
                SELECT (stat:covariance(?population, ?elevation) AS ?cov)
                WHERE {
                    ?city a dbo:City ;
                        dbo:populationTotal ?population .
                    OPTIONAL { ?city dbo:elevation ?elevation }
                }
                """, "cov");
    }

    @Test
    public void tinyEmpty_covarianceZero() {
        assertTinyCovarianceZero("empty");
    }

    @Test
    public void tinySingle_covarianceZero() {
        assertTinyCovarianceZero("single");
    }

    @Test
    public void tinyIdentical_covarianceZero() {
        assertTinyCovarianceZero("identical");
    }

    @Test
    public void tinyConstantX_covarianceZero() {
        assertTinyCovarianceZero("constantX");
    }

    @Test
    public void tinyTwo_covarianceOne() {
        assertEquals(1.0, query1Group1Var("custom-data/edge_tiny.ttl", PREFIX + """
                SELECT (stat:covariance(?x, ?y) AS ?cov)
                WHERE {
                    ?s ex:case "two" ; ex:x ?x ; ex:y ?y .
                }
                """, "cov").doubleValue(), 1e-9);
    }
}

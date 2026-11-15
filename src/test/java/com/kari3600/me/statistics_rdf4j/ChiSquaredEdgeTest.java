package com.kari3600.me.statistics_rdf4j;

import org.junit.jupiter.api.Test;

import static com.kari3600.me.statistics_rdf4j.TestUtil.query1Group1Var;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Edge cases for {@code stat:chi_sq}.
 * Unlike pearson/covariance, unbound OPTIONAL args become null categories → still a number.
 * Fixture: custom-data/chi.
 */
public class ChiSquaredEdgeTest {

    private static final String CHI_PREFIX = """
            PREFIX ex: <http://example.org/>
            """;

    // --- healthy control ---

    @Test
    public void chiRequiredComplete() {
        assertEquals(0.0, query1Group1Var("custom-data/chi.ttl", CHI_PREFIX + """
                SELECT (stat:chi_sq(?gender, ?order) AS ?chi)
                WHERE {
                    ?person a ex:Person ;
                        ex:gender ?gender ;
                        ex:order ?order .
                }
                """, "chi").doubleValue(), 1e-9);
    }

    // --- edge cases ---

    @Test
    public void chiOptionalMissing() {
        assertEquals(5.25, query1Group1Var("custom-data/chi.ttl", CHI_PREFIX + """
                SELECT (stat:chi_sq(?gender, ?order) AS ?chi)
                WHERE {
                    ?person a ex:Person .
                    OPTIONAL { ?person ex:gender ?gender }
                    OPTIONAL { ?person ex:order ?order }
                }
                """, "chi").doubleValue(), 1e-9);
    }

    @Test
    public void chiOnlyIncompletes() {
        assertEquals(0.75, query1Group1Var("custom-data/chi.ttl", CHI_PREFIX + """
                SELECT (stat:chi_sq(?gender, ?order) AS ?chi)
                WHERE {
                    VALUES ?person { ex:PersonE ex:PersonF ex:PersonG }
                    ?person a ex:Person .
                    OPTIONAL { ?person ex:gender ?gender }
                    OPTIONAL { ?person ex:order ?order }
                }
                """, "chi").doubleValue(), 1e-9);
    }
}

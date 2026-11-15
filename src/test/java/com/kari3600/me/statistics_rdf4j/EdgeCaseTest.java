package com.kari3600.me.statistics_rdf4j;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.kari3600.me.statistics_rdf4j.TestUtil.query1Group1Var;
import static com.kari3600.me.statistics_rdf4j.TestUtil.queryAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Agenda #1: incomplete / null pairs — custom-data/edge_pairs.ttl, custom-data/edge_chi.ttl (+ EDGE_* cars/cities).
 * Agenda #2: tiny / empty groups — custom-data/edge_tiny.ttl.
 * Agenda #3: richer queries — custom-data/edge_groups.ttl (GROUP BY / FILTER / multi-agg).
 * Agenda #4: dirty / weird values — custom-data/edge_dirty.ttl.
 *
 * Observed semantics (RDF4J 6.2.0-M2 + this library):
 * For pearson/covariance/percentile/median/probability: unbound OPTIONAL args
 * type-error the collector → one row with the aggregate variable unbound.
 * For chi_sq: unbound args are treated as null categories → still a number.
 * Degenerate pearson → NaN; degenerate sample covariance → 0.0;
 * percentile with zero inputs → NPE from the approx collector.
 * Raw xsd:boolean flags into stat:p(?flag) type-error; use (?flag = true) instead.
 */
public class EdgeCaseTest {

    private static final String PREFIX = """
            PREFIX ex: <http://example.org/edge#>
            """;

    private static final String DBPEDIA = """
            PREFIX dbo: <http://dbpedia.org/ontology/>
            PREFIX dbp: <http://dbpedia.org/property/>
            """;

    /** Assert single result row with unbound aggregate variable. */
    private static void assertUnboundAggregate(String dataset, String query, String variable) {
        List<BindingSet> rows = queryAll(dataset, query);
        assertEquals(1, rows.size());
        assertFalse(rows.get(0).hasBinding(variable));
        assertNull(rows.get(0).getValue(variable));
    }

    private static void assertUnboundAggregate(String query, String variable) {
        assertUnboundAggregate("custom-data/edge_pairs.ttl", query, variable);
    }

    /**
     * Incomplete pair case [0,1] vs missing english: alice+bob have math, no english,
     * forced into the aggregate via OPTIONAL → unbound ?corr.
     */
    @Test
    public void optionalAllYMissing_pearson() {
        assertUnboundAggregate(PREFIX + """
                SELECT (stat:pearson(?math, ?english) AS ?corr)
                WHERE {
                    VALUES ?student { ex:alice ex:bob }
                    ?student ex:math ?math .
                    OPTIONAL { ?student ex:english ?english }
                }
                """, "corr");
    }

    @Test
    public void optionalAllYMissing_covariance() {
        assertUnboundAggregate(PREFIX + """
                SELECT (stat:covariance(?math, ?english) AS ?cov)
                WHERE {
                    VALUES ?student { ex:alice ex:bob }
                    ?student ex:math ?math .
                    OPTIONAL { ?student ex:english ?english }
                }
                """, "cov");
    }

    /**
     * Mixed complete + incomplete under OPTIONAL: one unbound row type-errors
     * the collector and the whole aggregate comes back unbound (poisons completes).
     */
    @Test
    public void optionalMixed_pearsonPoisonsOnUnbound() {
        assertUnboundAggregate(PREFIX + """
                SELECT (stat:pearson(?math, ?english) AS ?corr)
                WHERE {
                    ?student a ex:Student ;
                        ex:math ?math .
                    OPTIONAL { ?student ex:english ?english }
                }
                """, "corr");
    }

    @Test
    public void optionalMixed_covariancePoisonsOnUnbound() {
        assertUnboundAggregate(PREFIX + """
                SELECT (stat:covariance(?math, ?english) AS ?cov)
                WHERE {
                    ?student a ex:Student ;
                        ex:math ?math .
                    OPTIONAL { ?student ex:english ?english }
                }
                """, "cov");
    }

    /**
     * REQUIRED both subjects: incompletes are join-dropped.
     * cara/dan/ella/finn lie on english = 2*math → pearson 1.0.
     */
    @Test
    public void requiredPerfectLine_pearson() {
        assertEquals(1.0, query1Group1Var("custom-data/edge_pairs.ttl", PREFIX + """
                SELECT (stat:pearson(?math, ?english) AS ?corr)
                WHERE {
                    VALUES ?student { ex:cara ex:dan ex:ella ex:finn }
                    ?student ex:math ?math ;
                             ex:english ?english .
                }
                """, "corr").doubleValue(), 1e-9);
    }

    @Test
    public void requiredPerfectLine_covariance() {
        // sample cov of [2,3,4,10] and [4,6,8,20]
        assertEquals(25.833333333333332, query1Group1Var("custom-data/edge_pairs.ttl", PREFIX + """
                SELECT (stat:covariance(?math, ?english) AS ?cov)
                WHERE {
                    VALUES ?student { ex:cara ex:dan ex:ella ex:finn }
                    ?student ex:math ?math ;
                             ex:english ?english .
                }
                """, "cov").doubleValue(), 1e-6);
    }

    /**
     * All complete pairs including off-line iris+jake → corr ≠ 1.0.
     * Incomplete students never match the REQUIRED pattern.
     */
    @Test
    public void requiredAllComplete_pearson() {
        assertEquals(0.45191786568701003, query1Group1Var("custom-data/edge_pairs.ttl", PREFIX + """
                SELECT (stat:pearson(?math, ?english) AS ?corr)
                WHERE {
                    ?student a ex:Student ;
                        ex:math ?math ;
                        ex:english ?english .
                }
                """, "corr").doubleValue(), 1e-6);
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

    /**
     * REQUIRED on alice+bob alone: no rows match → empty aggregate.
     * Document actual outcome (unbound or non-finite).
     */
    @Test
    public void requiredIncompleteOnly_emptyAggregate() {
        List<BindingSet> rows = queryAll("custom-data/edge_pairs.ttl", PREFIX + """
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

    // --- EDGE_*: natural datasets + other aggregates (OPTIONAL incompletes) ---

    private static final String CHI_PREFIX = """
            PREFIX ex: <http://example.org/>
            """;

    /** cars.ttl: length without weight → OPTIONAL poisons pearson. */
    @Test
    public void EDGE_carsOptionalMissingWeight_pearson() {
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

    /** Contrast: REQUIRED weight still yields the known happy-path covariance. */
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

    /** cities.ttl: population without elevation. */
    @Test
    public void EDGE_citiesOptionalMissingElevation_pearson() {
        assertUnboundAggregate("cities.ttl", DBPEDIA + """
                SELECT (stat:pearson(?population, ?elevation) AS ?corr)
                WHERE {
                    ?city a dbo:City ;
                        dbo:populationTotal ?population .
                    OPTIONAL { ?city dbo:elevation ?elevation }
                }
                """, "corr");
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
    public void EDGE_citiesOptionalMissingElevation_probability() {
        assertUnboundAggregate("cities.ttl", DBPEDIA + """
                SELECT (stat:p(?elevation > 1000) AS ?prob)
                WHERE {
                    ?city a dbo:City .
                    OPTIONAL { ?city dbo:elevation ?elevation }
                }
                """, "prob");
    }

    /** Handmade edge_pairs: univariate / boolean with OPTIONAL english. */
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
    public void EDGE_edgePairsOptionalEnglish_approxMedian() {
        assertUnboundAggregate("custom-data/edge_pairs.ttl", PREFIX + """
                SELECT (stat:approx_median(?english) AS ?med)
                WHERE {
                    ?student a ex:Student .
                    OPTIONAL { ?student ex:english ?english }
                }
                """, "med");
    }

    /**
     * chi_sq differs from pearson/covariance: unbound OPTIONAL args are treated
     * as null categories and still produce a number (does not poison to unbound).
     */
    @Test
    public void EDGE_chiOptionalMissing_chiSq() {
        assertEquals(5.25, query1Group1Var("custom-data/edge_chi.ttl", CHI_PREFIX + """
                SELECT (stat:chi_sq(?gender, ?order) AS ?chi)
                WHERE {
                    ?person a ex:Person .
                    OPTIONAL { ?person ex:gender ?gender }
                    OPTIONAL { ?person ex:order ?order }
                }
                """, "chi").doubleValue(), 1e-9);
    }

    /** Incomplete-only slice still yields a finite chi_sq (nulls as categories). */
    @Test
    public void EDGE_chiOnlyIncompletes_chiSq() {
        assertEquals(0.75, query1Group1Var("custom-data/edge_chi.ttl", CHI_PREFIX + """
                SELECT (stat:chi_sq(?gender, ?order) AS ?chi)
                WHERE {
                    VALUES ?person { ex:PersonE ex:PersonF ex:PersonG }
                    ?person a ex:Person .
                    OPTIONAL { ?person ex:gender ?gender }
                    OPTIONAL { ?person ex:order ?order }
                }
                """, "chi").doubleValue(), 1e-9);
    }

    /** REQUIRED complete 2x2 balanced table → chi_sq 0 (independent). */
    @Test
    public void EDGE_chiRequiredComplete_chiSq() {
        assertEquals(0.0, query1Group1Var("custom-data/edge_chi.ttl", CHI_PREFIX + """
                SELECT (stat:chi_sq(?gender, ?order) AS ?chi)
                WHERE {
                    ?person a ex:Person ;
                        ex:gender ?gender ;
                        ex:order ?order .
                }
                """, "chi").doubleValue(), 1e-9);
    }

    // --- Agenda #2: custom-data/edge_tiny.ttl (empty / single / identical / constantX / two) ---

    private static void assertTinyPearsonNaN(String caseName) {
        double d = query1Group1Var("custom-data/edge_tiny.ttl", PREFIX + """
                SELECT (stat:pearson(?x, ?y) AS ?corr)
                WHERE {
                    ?s ex:case "%s" ;
                       ex:x ?x ;
                       ex:y ?y .
                }
                """.formatted(caseName), "corr").doubleValue();
        assertTrue(Double.isNaN(d), caseName + " pearson expected NaN, got " + d);
    }

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
    public void tinyEmpty_pearsonNaN() {
        assertTinyPearsonNaN("empty");
    }

    @Test
    public void tinyEmpty_covarianceZero() {
        assertTinyCovarianceZero("empty");
    }

    @Test
    public void tinyEmpty_percentileNpe() {
        // no ?x bindings in this slice → approx percentile NPE on empty digest
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
    public void tinySingle_pearsonNaN() {
        assertTinyPearsonNaN("single");
    }

    @Test
    public void tinySingle_covarianceZero() {
        assertTinyCovarianceZero("single");
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
    public void tinyIdentical_pearsonNaN() {
        assertTinyPearsonNaN("identical");
    }

    @Test
    public void tinyIdentical_covarianceZero() {
        assertTinyCovarianceZero("identical");
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
    public void tinyConstantX_pearsonNaN() {
        assertTinyPearsonNaN("constantX");
    }

    @Test
    public void tinyConstantX_covarianceZero() {
        assertTinyCovarianceZero("constantX");
    }

    /** Smallest non-degenerate line: two points on y=2x → corr ~ 1, cov = 1. */
    @Test
    public void tinyTwo_pearsonOne() {
        assertEquals(1.0, query1Group1Var("custom-data/edge_tiny.ttl", PREFIX + """
                SELECT (stat:pearson(?x, ?y) AS ?corr)
                WHERE {
                    ?s ex:case "two" ; ex:x ?x ; ex:y ?y .
                }
                """, "corr").doubleValue(), 1e-9);
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

    @Test
    public void tinyEmpty_probabilityNaN() {
        // zero rows into p(...) → matches/total = 0/0 → NaN
        double d = query1Group1Var("custom-data/edge_tiny.ttl", PREFIX + """
                SELECT (stat:p(?x > 0) AS ?prob)
                WHERE {
                    VALUES ?s { ex:empty2 ex:empty3 }
                    ?s ex:x ?x .
                }
                """, "prob").doubleValue();
        assertTrue(Double.isNaN(d), "expected NaN for empty probability, got " + d);
    }

    // --- Agenda #3: richer queries (custom-data/edge_groups.ttl) ---

    @Test
    public void richerGroupBy_pearsonPerGroup() {
        List<BindingSet> rows = queryAll("custom-data/edge_groups.ttl", PREFIX + """
                SELECT ?g (stat:pearson(?math, ?english) AS ?corr) (COUNT(*) AS ?n)
                WHERE {
                    ?s ex:group ?g ;
                       ex:math ?math ;
                       ex:english ?english .
                }
                GROUP BY ?g
                ORDER BY ?g
                """);
        // group D has no complete pairs → absent from results
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
    public void richerFilterEmpties_pearsonNaN() {
        double d = query1Group1Var("custom-data/edge_groups.ttl", PREFIX + """
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

    @Test
    public void richerMultiAggregate_pearsonCovCount() {
        List<BindingSet> rows = queryAll("custom-data/edge_groups.ttl", PREFIX + """
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

    // --- Agenda #4: dirty / weird values (custom-data/edge_dirty.ttl) ---

    @Test
    public void dirtyStringNumber_percentileUnbound() {
        // plain string "10" is not an XSD numeric → type error → unbound
        assertUnboundAggregate("custom-data/edge_dirty.ttl", PREFIX + """
                SELECT (stat:percentile(?val, 0.5) AS ?p)
                WHERE {
                    ?s ex:case "stringNum" ; ex:val ?val .
                }
                """, "p");
    }

    @Test
    public void dirtyMixedNonNumeric_pearsonUnbound() {
        assertUnboundAggregate("custom-data/edge_dirty.ttl", PREFIX + """
                SELECT (stat:pearson(?val, ?other) AS ?corr)
                WHERE {
                    ?s ex:case "mixed" ; ex:val ?val ; ex:other ?other .
                }
                """, "corr");
    }

    @Test
    public void dirtyRawBooleanFlag_probabilityUnbound() {
        // CollectorUtil.asBoolean requires BooleanLiteral, not plain xsd:boolean
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
    public void dirtyDuplicateValues_percentile() {
        // multi-valued ex:val 1,1,2 on one subject → three observations
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

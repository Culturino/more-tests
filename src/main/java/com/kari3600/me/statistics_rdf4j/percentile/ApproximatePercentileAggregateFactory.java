package com.kari3600.me.statistics_rdf4j.percentile;

import com.kari3600.me.statistics_rdf4j.SimpleAggregateNAryFunction;
import com.kari3600.me.statistics_rdf4j.StatisticsRDF4J;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.sparql.aggregate.AggregateCollector;
import org.eclipse.rdf4j.query.parser.sparql.aggregate.AggregateNAryFunction;
import org.eclipse.rdf4j.query.parser.sparql.aggregate.AggregateNAryFunctionFactory;

import java.util.function.BiFunction;

public class ApproximatePercentileAggregateFactory implements AggregateNAryFunctionFactory {
    private static final int ARITY = 2;

    @Override
    public String getIri() {
        // PercentileTest / SPARQL use stat:percentile (approx impl under the hood)
        return StatisticsRDF4J.IRI + "percentile";
    }

    @Override
    public int getMinNumberOfArguments() {
        return ARITY;
    }

    @Override
    public int getMaxNumberOfArguments() {
        return ARITY;
    }

    @Override
    public AggregateNAryFunction buildFunction(BiFunction<Integer, BindingSet, Value> biFunction) {
        return new SimpleAggregateNAryFunction(biFunction, ARITY);
    }

    @Override
    public AggregateCollector getCollector() {
        return new ApproximatePercentileCollector();
    }
}

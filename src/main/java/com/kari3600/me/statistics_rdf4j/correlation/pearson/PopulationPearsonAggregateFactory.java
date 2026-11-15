package com.kari3600.me.statistics_rdf4j.correlation.pearson;

import com.kari3600.me.statistics_rdf4j.SimpleAggregateNAryFunction;
import com.kari3600.me.statistics_rdf4j.StatisticsRDF4J;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.sparql.aggregate.AggregateCollector;
import org.eclipse.rdf4j.query.parser.sparql.aggregate.AggregateNAryFunction;
import org.eclipse.rdf4j.query.parser.sparql.aggregate.AggregateNAryFunctionFactory;

import java.util.function.BiFunction;

public class PopulationPearsonAggregateFactory implements AggregateNAryFunctionFactory {
    private static final int ARITY = 2;

    @Override
    public String getIri() {
        return StatisticsRDF4J.IRI + "pearson_population";
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
        return new PearsonCollector(true, ARITY);
    }
}

package com.kari3600.me.statistics_rdf4j.covariance;

import com.kari3600.me.statistics_rdf4j.SimpleAggregateNAryFunction;
import com.kari3600.me.statistics_rdf4j.StatisticsRDF4J;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.sparql.aggregate.*;

import java.util.function.BiFunction;

public class CovarianceAggregateFactory implements AggregateNAryFunctionFactory {
	private static final int ARITY = 2;

	@Override
	public String getIri() {
		return StatisticsRDF4J.IRI + "covariance";
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
	public AggregateNAryFunction buildFunction(BiFunction<Integer, BindingSet, Value> evaluationStepByIndex) {
		return new SimpleAggregateNAryFunction(evaluationStepByIndex, ARITY);
	}

	@Override
	public AggregateCollector getCollector() {
		return new CovarianceCollector(false, ARITY);
	}
}

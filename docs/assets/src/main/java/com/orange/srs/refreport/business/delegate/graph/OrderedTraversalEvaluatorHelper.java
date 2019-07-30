package com.orange.srs.refreport.business.delegate.graph;

import java.util.List;

import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;

public class OrderedTraversalEvaluatorHelper implements Evaluator {

	private List<RelationshipType> orderedPathContext;

	public OrderedTraversalEvaluatorHelper(List<RelationshipType> orderedPathContext) {
		super();
		this.orderedPathContext = orderedPathContext;
	}

	@Override
	public Evaluation evaluate(Path currentPath) {
		if (currentPath.length() == 0) {
			return Evaluation.EXCLUDE_AND_CONTINUE;
		}

		RelationshipType expectedType = orderedPathContext.get(currentPath.length() - 1);

		boolean isExpectedType = currentPath.lastRelationship().isType(expectedType);
		boolean included = currentPath.length() == orderedPathContext.size() && isExpectedType;
		boolean continued = currentPath.length() < orderedPathContext.size() && isExpectedType;

		return Evaluation.of(included, continued);
	}

}

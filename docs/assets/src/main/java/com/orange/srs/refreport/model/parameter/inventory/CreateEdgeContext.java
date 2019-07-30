package com.orange.srs.refreport.model.parameter.inventory;

import com.orange.srs.refreport.business.delegate.graph.EdgeCreationCommandRegistry;
import com.orange.srs.refreport.technical.graph.AutoCommitTransaction;
import com.orange.srs.refreport.technical.graph.GraphDatabaseServiceWithStateProxy;

public class CreateEdgeContext {
	public GraphDatabaseServiceWithStateProxy graphDatabaseService;
	public CheckIfLinkFulfillRuleParameter checkIfLinkFulfillRuleParameter;
	public AutoCommitTransaction transaction;
	public EdgeCreationCommandRegistry edgeCreationCommandRegistry;
	public int numberOfEdgesCreated = 0;
	public int numberOfEdgesCreationError = 0;

	public CreateEdgeContext(GraphDatabaseServiceWithStateProxy graphDatabaseService,
			CheckIfLinkFulfillRuleParameter checkIfLinkFulfillRuleParameter, AutoCommitTransaction transaction) {
		super();
		this.graphDatabaseService = graphDatabaseService;
		this.checkIfLinkFulfillRuleParameter = checkIfLinkFulfillRuleParameter;
		this.transaction = transaction;
	}

}

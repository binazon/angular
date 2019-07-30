package com.orange.srs.refreport.model.parameter.inventory;

import com.orange.srs.refreport.technical.graph.AutoCommitTransaction;
import com.orange.srs.refreport.technical.graph.GraphDatabaseServiceWithStateProxy;

public class CreateNodeContext {
	public GraphDatabaseServiceWithStateProxy graphDatabaseService;
	public AutoCommitTransaction transaction;
	public int numberOfNodesCreated = 0;
	public int numberOfNodesCreationError = 0;

	public CreateNodeContext(GraphDatabaseServiceWithStateProxy graphDatabaseService,
			AutoCommitTransaction transaction) {
		super();
		this.graphDatabaseService = graphDatabaseService;
		this.transaction = transaction;
	}
}

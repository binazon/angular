package com.orange.srs.refreport.technical.graph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Lock;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;

public class AutoCommitTransaction implements Transaction {

	private GraphDatabaseService databaseService;
	private Transaction innerTransaction;
	private int currentNumberOfCommit;
	private int autoCommithreshold;

	public Lock acquireReadLock(PropertyContainer arg0) {
		return innerTransaction.acquireReadLock(arg0);
	}

	public Lock acquireWriteLock(PropertyContainer arg0) {
		return innerTransaction.acquireWriteLock(arg0);
	}

	public void failure() {
		innerTransaction.failure();
	}

	public void finish() {
		currentNumberOfCommit = 0;
		innerTransaction.finish();
	}

	public void autoCommit() {
		currentNumberOfCommit++;
		if (currentNumberOfCommit == autoCommithreshold) {
			innerTransaction.success();
			innerTransaction.finish();
			currentNumberOfCommit = 0;
			innerTransaction = databaseService.beginTx();
		}
	}

	public void flushTransaction() {
		success();
		finish();
		innerTransaction = databaseService.beginTx();
	}

	public void success() {
		innerTransaction.success();
	}

	protected static AutoCommitTransaction makeAutoCommitTransaction(int autoCommitThresholdParam,
			GraphDatabaseService databaseServiceParam) {
		AutoCommitTransaction createdObject = new AutoCommitTransaction();
		createdObject.databaseService = databaseServiceParam;
		createdObject.innerTransaction = databaseServiceParam.beginTx();
		createdObject.autoCommithreshold = autoCommitThresholdParam;

		return createdObject;
	}

}

package com.orange.srs.refreport.technical.graph;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.ReadableIndex;
import org.neo4j.graphdb.index.UniqueFactory;

import com.orange.srs.refreport.model.TO.inventory.GraphCreationStatusTO;
import com.orange.srs.refreport.model.enumerate.InventoryGraphEdgeTypeEnum;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.inventory.EdgeToTraverseDefinitionParameter;
import com.orange.srs.statcommon.model.parameter.inventory.GetHierarchyForEntityByLevelParameter;

public class GraphDatabaseServiceWithStateProxy {
	private GraphDatabaseService proxyedGraphService;
	private Date creationDate;
	private Date activationDate;
	private Date inactivationDate;
	private int currentNumberOfClients = 0;
	private GraphServiceStateEnum state;
	private GraphServerRegistry builder;
	private File directory;
	private ReadableIndex<Node> nodesAutomaticIndex;
	private GraphCreationStatusTO information = new GraphCreationStatusTO();

	public GraphDatabaseServiceWithStateProxy(GraphDatabaseService pservice, GraphServerRegistry pbuilder,
			File pdirectory) {
		super();
		this.builder = pbuilder;
		this.proxyedGraphService = pservice;
		creationDate = new Date();
		inactivationDate = null;
		activationDate = null;
		state = GraphServiceStateEnum.NEW;
		nodesAutomaticIndex = this.proxyedGraphService.index().getNodeAutoIndexer().getAutoIndex();
		directory = pdirectory;
	}

	public synchronized void activate(SOAContext context) throws BusinessException {
		changeState(GraphServiceStateEnum.ACTIVE, context);
	}

	protected synchronized void inactivate(SOAContext context) throws BusinessException {
		inactivationDate = new Date();
		state = GraphServiceStateEnum.INACTIVE;
		checkIfProxyMustBeGarbaged(context);
	}

	public synchronized void release(SOAContext context) throws BusinessException {
		this.currentNumberOfClients--;
		checkIfProxyMustBeGarbaged(context);
	}

	protected synchronized void addClient() {
		if (state == GraphServiceStateEnum.ACTIVE) {
			this.currentNumberOfClients++;
		}
	}

	private synchronized void checkIfProxyMustBeGarbaged(SOAContext context) throws BusinessException {
		if (getCurrentNumberOfClients() <= 0 && state == GraphServiceStateEnum.INACTIVE) {
			changeState(GraphServiceStateEnum.TO_GARBAGE, context);
		}
	}

	private void changeState(GraphServiceStateEnum newState, SOAContext context) throws BusinessException {
		if (newState == GraphServiceStateEnum.ACTIVE && state == GraphServiceStateEnum.NEW) {
			state = newState;
			activationDate = new Date();
			builder.activateGraphService(this, context);
		} else if (newState == GraphServiceStateEnum.TO_GARBAGE) {
			state = GraphServiceStateEnum.TO_GARBAGE;
			builder.garbageGraphService(this, context);
		} else if (newState == GraphServiceStateEnum.INACTIVE && state == GraphServiceStateEnum.ACTIVE) {
			inactivationDate = new Date();
			state = newState;
			checkIfProxyMustBeGarbaged(context);
		} else {
			throw new BusinessException(
					BusinessException.ILLEGAL_GRAPH_DATABASE_SERVICE_STATE_TRANSISTION_EXCEPTION + " : old state= "
							+ state + " - newstate=" + newState,
					BusinessException.ILLEGAL_GRAPH_DATABASE_SERVICE_STATE_TRANSISTION_CODE);
		}
	}

	public GraphDatabaseService getService() {
		return proxyedGraphService;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Date getActivationDate() {
		return activationDate;
	}

	public Date getInactivationDate() {
		return inactivationDate;
	}

	public int getCurrentNumberOfClients() {
		return currentNumberOfClients;
	}

	public GraphServiceStateEnum getState() {
		return state;
	}

	public enum GraphServiceStateEnum {
		NEW("NEW"), ACTIVE("ACTIVE"), INACTIVE("INACTIVE"), TO_GARBAGE("Both");

		private final String value;

		private GraphServiceStateEnum(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	public File getDirectory() {
		return directory;
	}

	public Transaction beginTransaction() {
		return proxyedGraphService.beginTx();
	}

	public AutoCommitTransaction beginAutoCommitedTransaction(int autoCommitThreshold) {
		return AutoCommitTransaction.makeAutoCommitTransaction(autoCommitThreshold, this.getService());
	}

	public Node createNode() {
		return proxyedGraphService.createNode();
	}

	public Node createIfNotExistsNodeThreadHostile(String uniqueIndexPropertyName, String propertyValue)
			throws BusinessException {
		IndexHits<Node> nodesFound = this.proxyedGraphService.index().getNodeAutoIndexer().getAutoIndex()
				.get(uniqueIndexPropertyName, propertyValue);
		if (nodesFound.size() == 0) {
			return createNode();
		} else {
			throw new BusinessException(
					BusinessException.ENTITY_NOT_UNIQUE_EXCEPTION = " node with id " + propertyValue,
					BusinessException.ENTITY_NOT_UNIQUE_EXCEPTION_CODE);
		}
	}

	private Map<String, UniqueFactory<Node>> nodeUniqueIndexMap = new HashMap<String, UniqueFactory<Node>>();

	public Node getOrCreateUserWithUniqueFactory(final String indexPropertyName, String indexPropertyValue) {
		UniqueFactory<Node> factory = nodeUniqueIndexMap.get(indexPropertyName);
		if (factory == null) {
			factory = new UniqueFactory.UniqueNodeFactory(getService(), indexPropertyName) {
				@Override
				protected void initialize(Node created, Map<String, Object> properties) {
					created.setProperty(indexPropertyName, properties.get(indexPropertyName));
				}
			};

			nodeUniqueIndexMap.put(indexPropertyName, factory);
		}
		return factory.getOrCreate(indexPropertyName, indexPropertyValue);
	}

	public Node getNodeById(long arg0) {
		return proxyedGraphService.getNodeById(arg0);
	}

	public Node getUniqueNodeByIndexedProperty(String nodeIndexName, String nodePropertyValue) {
		IndexHits<Node> nodesFound = this.proxyedGraphService.index().getNodeAutoIndexer().getAutoIndex()
				.get(nodeIndexName, nodePropertyValue);
		Node resultNode = nodesFound.getSingle();

		nodesFound.close();
		return resultNode;
	}

	public Node getUniqueNodeByHierarchyForEntity(String nodeIndexName,
			GetHierarchyForEntityByLevelParameter parameter) {
		IndexHits<Node> nodesFound = this.proxyedGraphService.index().getNodeAutoIndexer().getAutoIndex()
				.get(nodeIndexName, parameter.entityId);
		Node node = nodesFound.getSingle();

		// From this node we have to find the one higher in the hierarchy (configuration
		// in bookmark)
		Node resultNode = null;
		if (node != null) {
			resultNode = findTargetNode(node, parameter.edgeToTraverseDefinitions);
		}

		nodesFound.close();
		return resultNode != null ? resultNode : node;
	}

	private Node findTargetNode(Node node, Set<EdgeToTraverseDefinitionParameter> edgesToTraverse) {
		Node resultNode = null;
		for (EdgeToTraverseDefinitionParameter edgeToTraverse : edgesToTraverse) {
			Relationship rs = node.getSingleRelationship(InventoryGraphEdgeTypeEnum.valueOf(edgeToTraverse.edgeType),
					Direction.valueOf(edgeToTraverse.edgeOrientation).reverse());
			if (rs != null) {
				switch (Direction.valueOf(edgeToTraverse.edgeOrientation)) {
				case OUTGOING:
					resultNode = rs.getStartNode();
					break;
				case INCOMING:
					resultNode = rs.getEndNode();
					break;
				default:
					resultNode = rs.getStartNode();
					break;
				}
				if (!edgeToTraverse.target) {
					return findTargetNode(resultNode, edgeToTraverse.getNextEdgeToTraverseDefinition());
				}
			}
		}
		return resultNode;
	}

	public Node[] getNodesByIndexedProperty(String nodeIndexName, String nodePropertyValue) {
		IndexHits<Node> nodesFound = nodesAutomaticIndex.get(nodeIndexName, nodePropertyValue);
		Node[] nodesArrayResult = new Node[nodesFound.size()];
		int nodesArrayResultIndex = 0;
		for (Node node : nodesFound) {
			nodesArrayResult[nodesArrayResultIndex] = node;
			nodesArrayResultIndex++;
		}

		nodesFound.close();
		return nodesArrayResult;
	}

	public Relationship getRelationshipById(long arg0) {
		return proxyedGraphService.getRelationshipById(arg0);
	}

	public IndexManager index() {
		return proxyedGraphService.index();
	}

	public Set<String> getIndexes() {
		Set<String> indexes = proxyedGraphService.index().getNodeAutoIndexer().getAutoIndexedProperties();
		return indexes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((directory == null) ? 0 : directory.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GraphDatabaseServiceWithStateProxy other = (GraphDatabaseServiceWithStateProxy) obj;
		if (directory == null) {
			if (other.directory != null)
				return false;
		} else if (!directory.equals(other.directory))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GraphDatabaseServiceWithStateProxy [creationDate=" + creationDate + ", activationDate=" + activationDate
				+ ", inactivationDate=" + inactivationDate + ", currentNumberOfClients=" + currentNumberOfClients
				+ ", state=" + state + ", directory=" + directory + ", information=" + information + "]";
	}

	public GraphCreationStatusTO getInformation() {
		return information;
	}

	public void setInformation(GraphCreationStatusTO information) {
		this.information = information;
	}

}

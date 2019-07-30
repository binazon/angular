package com.orange.srs.refreport.business.delegate.graph.builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Traverser;

import com.orange.srs.refreport.business.delegate.InventoryGraphDelegate;
import com.orange.srs.statcommon.model.TO.inventory.ResultEntityNodeTO;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class TraversalResultFromPathBuilder {
	private static final Logger LOGGER = Logger.getLogger(TraversalResultFromPathBuilder.class);

	public Map<Integer, Map<String, ResultEntityNodeTO>> buildHierarchyFromPathesTraversal(List<Traverser> traversers,
			SOAContext soaContext) {
		Map<Integer, Map<String, ResultEntityNodeTO>> nodesByLevelMap = new HashMap<Integer, Map<String, ResultEntityNodeTO>>();
		Map<Integer, Node> currentBranches = new HashMap<Integer, Node>();

		for (Traverser traverser : traversers) {
			int previousLevel = Integer.MAX_VALUE;
			Node startPathNode = null;

			for (Path path : traverser) {
				if (LOGGER.isDebugEnabled()) {
					String output = "At depth " + path.length() + " => "
							+ path.startNode().getProperty(InventoryGraphDelegate.ENTITYTYPE_PROPERTY) + ", "
							+ path.startNode().getProperty(InventoryGraphDelegate.ENTITYID_PROPERTY) + " == "
							+ path.startNode().getId() + "\n" + " => "
							+ path.endNode().getProperty(InventoryGraphDelegate.ENTITYTYPE_PROPERTY) + ", "
							+ path.endNode().getProperty(InventoryGraphDelegate.ENTITYID_PROPERTY) + " == "
							+ path.endNode().getId() + "\n";

					LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "Handling " + output));
				}

				if (path.length() == 0) {
					Node startNode = path.startNode();
					String entityId = InventoryGraphDelegate.getEntityIdForNode(startNode);
					String entityType = InventoryGraphDelegate.getEntityTypeForNode(startNode);
					getOrCreateNodeAtLevel(entityId, entityType, 0, nodesByLevelMap, soaContext);
					startPathNode = startNode;
					currentBranches.put(0, startNode);
				} else {
					if (startPathNode == null || previousLevel >= path.length()) {
						startPathNode = currentBranches.get(path.length() - 1);
					}

					Node endPathNode = path.endNode();

					String startEntityId = InventoryGraphDelegate.getEntityIdForNode(startPathNode);
					String startEntityType = InventoryGraphDelegate.getEntityTypeForNode(startPathNode);

					String endEntityId = InventoryGraphDelegate.getEntityIdForNode(endPathNode);
					String endEntityType = InventoryGraphDelegate.getEntityTypeForNode(endPathNode);

					ResultEntityNodeTO startNode = getOrCreateNodeAtLevel(startEntityId, startEntityType,
							path.length() - 1, nodesByLevelMap, soaContext);
					ResultEntityNodeTO endNode = getOrCreateNodeAtLevel(endEntityId, endEntityType, path.length(),
							nodesByLevelMap, soaContext);
					startNode.linkedEntityNodes.add(endNode);

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "Adding " + endNode + " to " + startNode));
					}

					currentBranches.put(path.length(), endPathNode);
					startPathNode = endPathNode;
				}

				previousLevel = path.length();
				// "At depth " + path.length() + " => " + path.endNode().getProperty(
				// InventoryGraphDelegate.ENTITYTYPE_PROPERTY ) + ", "+
				// path.endNode().getProperty( InventoryGraphDelegate.ENTITYID_PROPERTY)+" == "
				// +path.endNode().getId()+ "\n"
			}
		}
		return nodesByLevelMap;
	}

	public Set<ResultEntityNodeTO> buildGraphFromPathesTraversalAndReturnRootNodes(List<Traverser> traversers,
			SOAContext soaContext) {
		Map<String, ResultEntityNodeTO> allNodesCreatedByEntityId = new HashMap<String, ResultEntityNodeTO>();
		Set<ResultEntityNodeTO> rootNodesCreated = new HashSet<ResultEntityNodeTO>();

		for (Traverser traverser : traversers) {
			int previousLevel = Integer.MAX_VALUE;
			Node startPathNode = null;
			;
			Map<Integer, Node> currentBranches = new HashMap<Integer, Node>();

			for (Path path : traverser) {
				if (LOGGER.isDebugEnabled()) {
					String output = "At depth " + path.length() + " => "
							+ path.startNode().getProperty(InventoryGraphDelegate.ENTITYTYPE_PROPERTY) + ", "
							+ path.startNode().getProperty(InventoryGraphDelegate.ENTITYID_PROPERTY) + " == "
							+ path.startNode().getId() + "\n" + " => "
							+ path.endNode().getProperty(InventoryGraphDelegate.ENTITYTYPE_PROPERTY) + ", "
							+ path.endNode().getProperty(InventoryGraphDelegate.ENTITYID_PROPERTY) + " == "
							+ path.endNode().getId() + "\n";
					LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "Handling " + output));
				}

				if (path.length() == 0) {
					Node startNode = path.startNode();
					String entityId = InventoryGraphDelegate.getEntityIdForNode(startNode);
					String entityType = InventoryGraphDelegate.getEntityTypeForNode(startNode);
					ResultEntityNodeTO fisrtLevelNode = getOrCreateNodeInGraph(entityId, entityType,
							allNodesCreatedByEntityId, soaContext);
					rootNodesCreated.add(fisrtLevelNode);
					startPathNode = startNode;
					currentBranches.put(0, startNode);
				} else {
					if (startPathNode == null || previousLevel >= path.length()) {
						// startPathNode=path.startNode();
						startPathNode = currentBranches.get(path.length() - 1);
					}

					Node endPathNode = path.endNode();

					String startEntityId = InventoryGraphDelegate.getEntityIdForNode(startPathNode);
					String startEntityType = InventoryGraphDelegate.getEntityTypeForNode(startPathNode);

					String endEntityId = InventoryGraphDelegate.getEntityIdForNode(endPathNode);
					String endEntityType = InventoryGraphDelegate.getEntityTypeForNode(endPathNode);

					ResultEntityNodeTO startNode = getOrCreateNodeInGraph(startEntityId, startEntityType,
							allNodesCreatedByEntityId, soaContext);
					ResultEntityNodeTO endNode = getOrCreateNodeInGraph(endEntityId, endEntityType,
							allNodesCreatedByEntityId, soaContext);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "Adding " + endNode + " to " + startNode));
					}

					startNode.linkedEntityNodes.add(endNode);
					currentBranches.put(path.length(), endPathNode);
					startPathNode = endPathNode;
				}

				previousLevel = path.length();
			}
		}
		return rootNodesCreated;
	}

	/*
	 * public Set<ResultEntityNodeTO> buildGraphFromRelationshipsTraversalAndReturnRootNodes(List<Traverser> traversers,
	 * Set<String> rootEntityIds) {
	 * 
	 * SOAContext soaContext=SOATools.buildSOAContext(null); Map<String,ResultEntityNodeTO>
	 * allNodesCreatedByEntityId=new HashMap<String, ResultEntityNodeTO>(); Set<ResultEntityNodeTO> rootNodesCreated=new
	 * HashSet<ResultEntityNodeTO>();
	 * 
	 * for(Traverser traverser:traversers) { for(Path path : traverser) {
	 * System.out.println("Handling 1 path startNode="+InventoryGraphDelegate.
	 * getEntityIdForNode(path.startNode())+", endNode="+InventoryGraphDelegate. getEntityIdForNode(path.endNode()));
	 * 
	 * for(Relationship relationship : path.relationships()) { if (LOGGER.isDebugEnabled()) {
	 * LOGGER.debug("Handling 12 relationship startNode="+InventoryGraphDelegate.
	 * getEntityIdForNode(relationship.getStartNode())+", endNode="
	 * +InventoryGraphDelegate.getEntityIdForNode(relationship.getEndNode()));
	 * System.out.println("Handling 12 relationship startNode="
	 * +InventoryGraphDelegate.getEntityIdForNode(relationship.getStartNode())
	 * +", endNode="+InventoryGraphDelegate.getEntityIdForNode(relationship. getEndNode())); }
	 * 
	 * Node startPathNode=relationship.getStartNode(); Node endPathNode=relationship.getEndNode();
	 * 
	 * String startEntityId=InventoryGraphDelegate.getEntityIdForNode(startPathNode); String
	 * startEntityType=InventoryGraphDelegate.getEntityTypeForNode(startPathNode);
	 * 
	 * String endEntityId=InventoryGraphDelegate.getEntityIdForNode(endPathNode); String
	 * endEntityType=InventoryGraphDelegate.getEntityTypeForNode(endPathNode);
	 * 
	 * ResultEntityNodeTO startNode=getOrCreateNodeInGraph(startEntityId, startEntityType, allNodesCreatedByEntityId,
	 * soaContext); ResultEntityNodeTO endNode=getOrCreateNodeInGraph(endEntityId, endEntityType,
	 * allNodesCreatedByEntityId, soaContext);
	 * 
	 * if(!rootEntityIds.contains(startEntityId)) { rootNodesCreated.add(startNode); }
	 * 
	 * if(!rootEntityIds.contains(endEntityId)) { rootNodesCreated.add(endNode); }
	 * 
	 * startNode.linkedEntityNodes.add(endNode); } } } return rootNodesCreated; }
	 */

	private ResultEntityNodeTO getOrCreateNodeAtLevel(String entityId, String entityType, int level,
			Map<Integer, Map<String, ResultEntityNodeTO>> nodesByLevelMap, SOAContext soaContext) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "Checking " + entityId + " Al level " + level));
		}

		ResultEntityNodeTO resultNode;
		Map<String, ResultEntityNodeTO> levelFound = nodesByLevelMap.get(level);

		if (levelFound == null) {
			levelFound = new HashMap<String, ResultEntityNodeTO>();
			nodesByLevelMap.put(level, levelFound);
		}

		resultNode = levelFound.get(entityId);

		if (resultNode == null) {
			resultNode = new ResultEntityNodeTO();
			resultNode.entityId = entityId;
			resultNode.entityType = entityType;
			levelFound.put(entityId, resultNode);
		}

		return resultNode;
	}

	private ResultEntityNodeTO getOrCreateNodeInGraph(String entityId, String entityType,
			Map<String, ResultEntityNodeTO> nodesByEntityIdMap, SOAContext soaContext) {
		ResultEntityNodeTO resultNode = nodesByEntityIdMap.get(entityId);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "Handling node " + entityId));
		}

		if (resultNode == null) {
			resultNode = new ResultEntityNodeTO();
			resultNode.entityId = entityId;
			resultNode.entityType = entityType;
			nodesByEntityIdMap.put(entityId, resultNode);
		}

		return resultNode;
	}

}

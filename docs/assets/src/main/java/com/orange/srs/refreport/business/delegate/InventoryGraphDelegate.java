package com.orange.srs.refreport.business.delegate;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Node;

import com.orange.srs.refreport.business.delegate.graph.EdgeCreationWithRuleCommand;
import com.orange.srs.refreport.model.enumerate.InventoryGraphEdgeTypeEnum;
import com.orange.srs.refreport.model.parameter.inventory.CheckIfLinkFulfillRuleParameter;
import com.orange.srs.refreport.model.parameter.inventory.CreateEdgeContext;
import com.orange.srs.refreport.model.parameter.inventory.CreateEdgeParameter;
import com.orange.srs.refreport.model.parameter.inventory.CreateNodeContext;
import com.orange.srs.refreport.model.parameter.inventory.CreateNodeParameter;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refreport.technical.graph.AutoCommitTransaction;
import com.orange.srs.refreport.technical.graph.GraphDatabaseServiceWithStateProxy;
import com.orange.srs.refreport.technical.graph.GraphServerRegistry;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class InventoryGraphDelegate {

	private static final Logger LOGGER = Logger.getLogger(InventoryGraphDelegate.class);

	public static final String ENTITYID_PROPERTY = "entityid";
	public static final String ENTITYTYPE_PROPERTY = "entitytype";

	public static final FastDateFormat DB_DATE_FORMATTER = FastDateFormat.getInstance("yyMMdd");
	public static final String DB_NAME = "inventoryGraphDatabase";

	private static final String[] NODE_INDEXES = { ENTITYTYPE_PROPERTY, ENTITYID_PROPERTY };

	public void createNodeForEntity(CreateNodeParameter createNodeParameter, CreateNodeContext createNodeContext)
			throws BusinessException {
		Node node = createNodeContext.graphDatabaseService.createIfNotExistsNodeThreadHostile(ENTITYID_PROPERTY,
				createNodeParameter.entityId);

		node.setProperty(ENTITYTYPE_PROPERTY, createNodeParameter.entityType);
		node.setProperty(ENTITYID_PROPERTY, createNodeParameter.entityId);

		createNodeContext.transaction.autoCommit();
	}

	public void createEdge(CreateEdgeParameter createEdgeParameter, CreateEdgeContext createEdgeContext,
			SOAContext soaContext) throws BusinessException {
		if (!isEdgeCreationParameterValid(createEdgeParameter)) {
			throw new BusinessException(
					BusinessException.WRONG_PARAMETER_EXCEPTION
							+ " : destination and origin infromation as well as role cannot be null",
					BusinessException.WRONG_PARAMETER_EXCEPTION_CODE);
		}

		CheckIfLinkFulfillRuleParameter checkIfLinkFulfillRuleParameter = createEdgeContext.checkIfLinkFulfillRuleParameter;
		AutoCommitTransaction transaction = createEdgeContext.transaction;

		boolean atLeastOneRuleVerified = false;

		List<EdgeCreationWithRuleCommand> edgeCreationWithRuleCommandList = createEdgeContext.edgeCreationCommandRegistry
				.getEdgeCreationCommandByType(createEdgeParameter.sourceType, createEdgeParameter.destinationType,
						createEdgeParameter.role);

		Node sourceNode = createEdgeContext.graphDatabaseService.getUniqueNodeByIndexedProperty(ENTITYID_PROPERTY,
				createEdgeParameter.sourceId);
		Node destinationNode = createEdgeContext.graphDatabaseService.getUniqueNodeByIndexedProperty(ENTITYID_PROPERTY,
				createEdgeParameter.destinationId);

		checkIfLinkFulfillRuleParameter.destinationType = createEdgeParameter.destinationType;
		checkIfLinkFulfillRuleParameter.originType = createEdgeParameter.sourceType;
		checkIfLinkFulfillRuleParameter.linkParameterValue = createEdgeParameter.parameter;

		for (EdgeCreationWithRuleCommand edgeCreationWithRuleCommand : edgeCreationWithRuleCommandList) {
			if (edgeCreationWithRuleCommand.checkIfLinkFulfillRule(createEdgeContext.checkIfLinkFulfillRuleParameter)) {
				atLeastOneRuleVerified = true;

				InventoryGraphEdgeTypeEnum edgeTypeCorrespondingToRule = edgeCreationWithRuleCommand
						.getEdgeCorrespondingType();
				if (sourceNode != null && destinationNode != null && edgeTypeCorrespondingToRule != null) {
					sourceNode.createRelationshipTo(destinationNode, edgeTypeCorrespondingToRule);
					transaction.autoCommit();
				} else {
					throw new BusinessException(
							BusinessException.EDGE_CREATION_EXCEPTION_MESSAGE + " : "
									+ "Not found - Cannot create relathionship between " + createEdgeParameter.sourceId
									+ "= " + sourceNode + " and " + createEdgeParameter.destinationId + " = "
									+ destinationNode + " with relationType " + edgeTypeCorrespondingToRule,
							BusinessException.EDGE_CREATION_EXCEPTION);
				}
			}
		}

		if (!atLeastOneRuleVerified) {
			String rulesApplied = "Rules Applied : ";
			for (EdgeCreationWithRuleCommand edgeCreationWithRuleCommand : edgeCreationWithRuleCommandList) {
				rulesApplied += edgeCreationWithRuleCommand.toString() + ";";
			}
			LOGGER.error(SOATools.buildSOALogMessage(soaContext,
					"Check not verified - Cannot create relathionship between " + createEdgeParameter.sourceId + " and "
							+ createEdgeParameter.destinationId + " with role =" + createEdgeParameter.role
							+ " and parameter = " + createEdgeParameter.parameter + "." + rulesApplied));
		}

	}

	public boolean isEdgeCreationParameterValid(CreateEdgeParameter createEdgeParameter) {
		if (createEdgeParameter == null) {
			return false;
		} else if (createEdgeParameter.destinationId == null || createEdgeParameter.destinationType == null
				|| createEdgeParameter.destinationOrigin == null) {
			return false;
		} else if (createEdgeParameter.sourceId == null || createEdgeParameter.sourceType == null
				|| createEdgeParameter.sourceOrigin == null) {
			return false;
		} else if (createEdgeParameter.role == null) {
			return false;
		}

		return true;
	}

	public GraphDatabaseServiceWithStateProxy createInventoryGraphDatabase(Date creationDate,
			GraphServerRegistry graphServerRegistry, SOAContext context) throws BusinessException, IOException {
		String graphDatabaseLocation = getGraphDatabaseFolder() + DB_NAME + DB_DATE_FORMATTER.format(creationDate)
				+ File.separator;

		LOGGER.info(SOATools.buildSOALogMessage(context, "Creating Database at location " + graphDatabaseLocation));

		GraphDatabaseServiceWithStateProxy graphService = graphServerRegistry
				.createGraphDatabaseService(graphDatabaseLocation, NODE_INDEXES, context);
		return graphService;
	}

	public static String[] getGraphDatabaseIndexes() {
		return NODE_INDEXES;
	}

	public static String getGraphDatabaseFolder() {
		return Configuration.rootProperty + File.separator + Configuration.graphdatabaseFolder + File.separator;
	}

	public static String getEntityIdForNode(Node node) {
		return node.getProperty(InventoryGraphDelegate.ENTITYID_PROPERTY).toString();
	}

	public static void setEntityIdForNode(Node node, String entityId) {
		node.setProperty(InventoryGraphDelegate.ENTITYID_PROPERTY, entityId);
	}

	public static String getEntityTypeForNode(Node node) {
		return node.getProperty(InventoryGraphDelegate.ENTITYTYPE_PROPERTY).toString();
	}

	public static void setEntityTypeForNode(Node node, String entityType) {
		node.setProperty(InventoryGraphDelegate.ENTITYTYPE_PROPERTY, entityType);
	}

}

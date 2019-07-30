package com.orange.srs.refreport.business;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.TransactionFailureException;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

import com.orange.srs.refreport.business.delegate.InventoryGraphDelegate;
import com.orange.srs.refreport.business.delegate.graph.EdgeCreationCommandRegistry;
import com.orange.srs.refreport.business.delegate.graph.builder.InventoryTraversalBuilder;
import com.orange.srs.refreport.business.delegate.graph.builder.TraversalResultFromPathBuilder;
import com.orange.srs.refreport.consumer.jdbc.ReportingEntityJDBCConsumer;
import com.orange.srs.refreport.consumer.jdbc.ReportingEntityJDBCConsumer.EntityAndParentWithIdAndTypeResultSet;
import com.orange.srs.refreport.consumer.jdbc.ReportingEntityJDBCConsumer.LinkWithExtremityIdAndOriginResultSet;
import com.orange.srs.refreport.model.TO.inventory.GetGraphDatabaseListTO;
import com.orange.srs.refreport.model.TO.inventory.GraphCreationStatusTO;
import com.orange.srs.refreport.model.parameter.inventory.AddEdgeCreationCommandParameter;
import com.orange.srs.refreport.model.parameter.inventory.CheckIfLinkFulfillRuleParameter;
import com.orange.srs.refreport.model.parameter.inventory.CreateEdgeContext;
import com.orange.srs.refreport.model.parameter.inventory.CreateEdgeParameter;
import com.orange.srs.refreport.model.parameter.inventory.CreateNodeContext;
import com.orange.srs.refreport.model.parameter.inventory.CreateNodeParameter;
import com.orange.srs.refreport.model.parameter.inventory.InventoryGraphCreationParameter;
import com.orange.srs.refreport.model.parameter.inventory.LinkEdgeDefinition;
import com.orange.srs.refreport.model.parameter.inventory.ParentEdgeDefinition;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refreport.technical.graph.AutoCommitTransaction;
import com.orange.srs.refreport.technical.graph.GraphDatabaseServiceWithStateProxy;
import com.orange.srs.refreport.technical.graph.GraphServerRegistry;
import com.orange.srs.refreport.technical.xml.JAXBRefReportFactory;
import com.orange.srs.statcommon.model.TO.JobTO.JobSummaryTO;
import com.orange.srs.statcommon.model.TO.inventory.ResultEntityNodeTO;
import com.orange.srs.statcommon.model.enums.JobEventCriticityEnum;
import com.orange.srs.statcommon.model.enums.JobEventTypeEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.inventory.GetHierarchyForEntityByLevelParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;

@Stateless
public class SOA16InventoryGraphFacade {

	private static final Logger LOGGER = Logger.getLogger(SOA16InventoryGraphFacade.class);

	private static final String PHYSICAL_PARENT_ROLE = "PHYSICAL_PARENT";

	private static final int AUTOCOMMIT_THRESHOLD = 5000;

	@EJB
	private GraphServerRegistry graphServerRegistry;

	@Inject
	private ReportingEntityJDBCConsumer reportingEntityJDBCConsumer;

	@EJB
	private InventoryGraphDelegate inventoryGraphDelegate;

	@EJB
	private InventoryTraversalBuilder inventoryTraversalBuilder;

	@EJB
	private TraversalResultFromPathBuilder traversalResultFromPathBuilder;

	public boolean testGraphDatabaseConnection() {

		SOAContext soaContext = SOATools.buildSOAContext(null);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(SOATools.buildSOALogMessage(soaContext,
					"GRAPHDATABASE FOLDER IS : " + InventoryGraphDelegate.getGraphDatabaseFolder()
							+ " AND CACHE POLICY IS " + Configuration.configGraphCachePolicy));
		}

		boolean testStatusOk = false;

		if (graphServerRegistry.getCurrentService() != null) {
			GraphDatabaseServiceWithStateProxy graphService = graphServerRegistry.getCurrentService();
			Node node = graphService.getNodeById(1);

			if (node != null) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(SOATools.buildSOALogMessage(soaContext,
							"Node " + node.getProperty(InventoryGraphDelegate.ENTITYID_PROPERTY) + " "
									+ node.getProperty(InventoryGraphDelegate.ENTITYTYPE_PROPERTY)));
				}
				testStatusOk = true;
			} else if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "No Node found for id 1"));
			}
		} else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "No current graphservice"));
		}
		return testStatusOk;
	}

	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public void createAndActivateInventoryGraphAsynchronously(Date creationDate, SOAContext soaContext) {
		try {
			createAndActivateInventoryGraph(new JobSummaryTO(), creationDate, soaContext);
		} catch (Exception bex) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext,
					"Failed to create inventoryGraph for " + creationDate.toString() + " - " + bex.getMessage()), bex);
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public GraphCreationStatusTO createAndActivateInventoryGraphSynchronously(JobSummaryTO jobSummaryTO,
			Date creationDate, SOAContext soaContext) throws JAXBException {
		return createAndActivateInventoryGraph(jobSummaryTO, creationDate, soaContext);
	}

	private GraphCreationStatusTO createAndActivateInventoryGraph(JobSummaryTO jobSummaryTO, Date creationDate,
			SOAContext soaContext) throws JAXBException {
		GraphCreationStatusTO creationResult = new GraphCreationStatusTO();

		Long graphCreationStartTime = Utils.getTime();

		Unmarshaller xmlUnmarshaller = JAXBRefReportFactory.getUnmarshaller();

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"GRAPH Creation STEP 1 Of 10 : Unmarshall graph creation configuration file : "
						+ Configuration.graphExportConfFileName));
		File file = new File(Configuration.graphExportConfFileName);
		InventoryGraphCreationParameter graphCreationParameter = (InventoryGraphCreationParameter) xmlUnmarshaller
				.unmarshal(file);
		jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.INFO,
				"DatabaseGraph : Unmarshall graph creation configuration file : "
						+ Configuration.graphExportConfFileName);

		EntityAndParentWithIdAndTypeResultSet entityWithParentResultSet = null;
		LinkWithExtremityIdAndOriginResultSet linkWithExtremityIdAndOriginResultSet = null;
		AutoCommitTransaction graphServiceTransaction = null;

		try {
			LOGGER.info(
					SOATools.buildSOALogMessage(soaContext, "GRAPH Creation STEP 2 Of 10 : Creating rules registry"));

			EdgeCreationCommandRegistry parentEdgeCreationCommandRegistry = createParentLinkEdgeCommandRegistry(
					graphCreationParameter.parentEdgeDefinition);
			EdgeCreationCommandRegistry complexEdgeCreationCommandRegistry = createComplexLinkEdgeCommandRegistry(
					graphCreationParameter.linkEdgeDefinition, soaContext);

			jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.INFO,
					"DatabaseGraph : Creating rules registry");
			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"GRAPH Creation STEP 3 Of 10 : getting connection to RefReport"));

			reportingEntityJDBCConsumer.openRefReportConnection();

			jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.INFO,
					"DatabaseGraph : getting connection to RefReport");
			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"GRAPH Creation STEP 4 Of 10 : executing request to retrieve entities and parents"));

			Long getEntityAndParentStart = Utils.getTime();
			entityWithParentResultSet = reportingEntityJDBCConsumer.getEntityAndParentWithIdAndType();// getEntityWithIdAndType(refReportConnection);
			Long getEntityAndParentEnd = Utils.getTime();

			creationResult.entityAndParentRequestDuration = getEntityAndParentEnd - getEntityAndParentStart;

			jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.INFO,
					"DatabaseGraph : executing request to retrieve entities and parents");
			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"GRAPH Creation STEP 5 Of 10 : creating Database for Date " + creationDate));

			GraphDatabaseServiceWithStateProxy graphService = inventoryGraphDelegate
					.createInventoryGraphDatabase(creationDate, graphServerRegistry, soaContext);

			graphServiceTransaction = graphService.beginAutoCommitedTransaction(AUTOCOMMIT_THRESHOLD);

			jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.INFO,
					"DatabaseGraph : creating Database for Date " + creationDate);
			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"GRAPH Creation STEP 6 Of 10 : inserting nodes for entity"));

			Long createEntityNodeStart = Utils.getTime();
			CreateNodeContext createNodeContext = new CreateNodeContext(graphService, graphServiceTransaction);
			createAllEntityNodes(entityWithParentResultSet, createNodeContext, soaContext);
			Long createEntityNodeEnd = Utils.getTime();

			creationResult.entityNodeCreationDuration = createEntityNodeEnd - createEntityNodeStart;
			creationResult.nbNodeCreated = createNodeContext.numberOfNodesCreated;
			creationResult.nbNodeCreationError = createNodeContext.numberOfNodesCreationError;

			graphServiceTransaction.flushTransaction();

			CheckIfLinkFulfillRuleParameter checkIfLinkFulfillRuleParameter = new CheckIfLinkFulfillRuleParameter();

			CreateEdgeContext createEdgeContext = new CreateEdgeContext(graphService, checkIfLinkFulfillRuleParameter,
					graphServiceTransaction);
			createEdgeContext.edgeCreationCommandRegistry = parentEdgeCreationCommandRegistry;

			jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.INFO,
					"DatabaseGraph : inserting nodes for entity");
			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"GRAPH Creation STEP 7 Of 10 : inserting parent edge for entity"));

			entityWithParentResultSet.beforeFirst();
			long createParentEdgeStart = Utils.getTime();
			createParentEdges(entityWithParentResultSet, createEdgeContext, soaContext);
			long createParentEdgeEnd = Utils.getTime();

			creationResult.parentEdgeCreationDuration = createParentEdgeEnd - createParentEdgeStart;
			creationResult.nbParentLinkCreated = createEdgeContext.numberOfEdgesCreated;
			creationResult.nbParentLinkCreationError = createEdgeContext.numberOfEdgesCreationError;

			graphServiceTransaction.flushTransaction();

			jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.INFO,
					"DatabaseGraph : inserting parent edge for entity");
			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"GRAPH Creation STEP 8 Of 10 : executing request to retrieve complex links"));

			Long complexLinkRequestStart = Utils.getTime();
			linkWithExtremityIdAndOriginResultSet = reportingEntityJDBCConsumer
					.getLinkWithExtremityIdAndOriginResultSet();
			Long complexLinkRequestEnd = Utils.getTime();

			creationResult.linkRequestDuration = complexLinkRequestEnd - complexLinkRequestStart;

			jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.INFO,
					"DatabaseGraph : executing request to retrieve complex links");
			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"GRAPH Creation STEP 9 Of 10 : inserting node for complex links"));

			createEdgeContext.edgeCreationCommandRegistry = complexEdgeCreationCommandRegistry;

			Long createComplexLinkStart = Utils.getTime();
			createComplexLinkEdges(linkWithExtremityIdAndOriginResultSet, createEdgeContext, soaContext);
			Long createComplexLinkEnd = Utils.getTime();

			creationResult.complexLinxEdgeCreationDuration = createComplexLinkEnd - createComplexLinkStart;
			creationResult.nbComplexLinkCreated = createEdgeContext.numberOfEdgesCreated;
			creationResult.nbComplexLinkCreationError = createEdgeContext.numberOfEdgesCreationError;

			jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.INFO,
					"DatabaseGraph : inserting node for complex links");
			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"GRAPH Creation STEP 10 Of 10 : activating graph and clearing connections to RefReport and transaction on GraphDatabase"));

			graphServiceTransaction.finish();
			graphService.setInformation(creationResult);
			graphService.activate(soaContext);

			jobSummaryTO.jobOutput = "DatabaseGraph exported in directory: "
					+ graphService.getDirectory().getAbsolutePath();
			jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.INFO,
					"DatabaseGraph : activating graph and clearing connections to RefReport and transaction on GraphDatabase");

		} catch (SQLException sqle) {
			String msg = "SQL error while creating DatabaseGraph for " + creationDate + ": " + sqle.getMessage();
			jobSummaryTO.jobOutput = msg;
			jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.ERROR, msg);
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, msg), sqle);

			if (graphServiceTransaction != null) {
				graphServiceTransaction.failure();
				graphServiceTransaction.finish();
			}
		} catch (Exception e) {
			String msg = "Unknown error while creating DatabaseGraph for " + creationDate + ": " + e.getMessage();
			jobSummaryTO.jobOutput = msg;
			jobSummaryTO.addEvent(JobEventTypeEnum.EXPORT, JobEventCriticityEnum.ERROR, msg);
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, msg), e);

			if (graphServiceTransaction != null) {
				graphServiceTransaction.failure();
				graphServiceTransaction.finish();
			}

		} finally {
			if (entityWithParentResultSet != null) {
				entityWithParentResultSet.close(soaContext);
			}

			if (linkWithExtremityIdAndOriginResultSet != null) {
				linkWithExtremityIdAndOriginResultSet.close(soaContext);
			}

			try {
				reportingEntityJDBCConsumer.closeRefReportConnection();
			} catch (Exception e) {
				LOGGER.warn(SOATools.buildSOALogMessage(soaContext,
						"Cannot close RefReport connexion (" + e.getMessage() + ")", e));
			}
		}

		Long graphCreationEndTime = Utils.getTime();
		creationResult.totalDuration = graphCreationEndTime - graphCreationStartTime;

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"DatabaseGraph creation successful : " + creationResult.toString()));

		return creationResult;
	}

	private EdgeCreationCommandRegistry createParentLinkEdgeCommandRegistry(
			List<ParentEdgeDefinition> parentEdgeDefinitions) throws Exception {
		EdgeCreationCommandRegistry parentEdgeCreationCommandRegistry = new EdgeCreationCommandRegistry();

		AddEdgeCreationCommandParameter addEdgeCreationCommandParameter = new AddEdgeCreationCommandParameter();

		for (ParentEdgeDefinition parentCreationParameter : parentEdgeDefinitions) {
			addEdgeCreationCommandParameter.associatedRule = "*";
			addEdgeCreationCommandParameter.originType = parentCreationParameter.parentEntityType;
			addEdgeCreationCommandParameter.destinationType = parentCreationParameter.childEntityType;
			addEdgeCreationCommandParameter.edgeCorrespondingType = parentCreationParameter.edgeType;
			addEdgeCreationCommandParameter.role = PHYSICAL_PARENT_ROLE;
			parentEdgeCreationCommandRegistry.addEdgeCreationCommand(addEdgeCreationCommandParameter);
			addEdgeCreationCommandParameter.clear();
		}

		return parentEdgeCreationCommandRegistry;
	}

	private EdgeCreationCommandRegistry createComplexLinkEdgeCommandRegistry(
			List<LinkEdgeDefinition> linkEdgeDefinitions, SOAContext context) throws Exception {
		EdgeCreationCommandRegistry complexEdgeCreationCommandRegistry = new EdgeCreationCommandRegistry();

		AddEdgeCreationCommandParameter addEdgeCreationCommandParameter = new AddEdgeCreationCommandParameter();

		for (LinkEdgeDefinition linkCreationParameter : linkEdgeDefinitions) {
			addEdgeCreationCommandParameter.associatedRule = linkCreationParameter.parameterRule;
			addEdgeCreationCommandParameter.destinationType = linkCreationParameter.destinationEntityType;
			addEdgeCreationCommandParameter.originType = linkCreationParameter.sourceEntityType;
			addEdgeCreationCommandParameter.edgeCorrespondingType = linkCreationParameter.edgeType;
			addEdgeCreationCommandParameter.role = linkCreationParameter.role;
			LOGGER.info(SOATools.buildSOALogMessage(context, "Adding role " + addEdgeCreationCommandParameter.role));
			complexEdgeCreationCommandRegistry.addEdgeCreationCommand(addEdgeCreationCommandParameter);
			addEdgeCreationCommandParameter.clear();
		}

		return complexEdgeCreationCommandRegistry;
	}

	private void createAllEntityNodes(EntityAndParentWithIdAndTypeResultSet entityWithParentResultSet,
			CreateNodeContext createNodeContext, SOAContext soaContext) throws SQLException {
		createNodeContext.numberOfNodesCreated = 0;
		createNodeContext.numberOfNodesCreationError = 0;

		CreateNodeParameter createNodeParameter = new CreateNodeParameter();

		while (entityWithParentResultSet.next()) {
			try {
				createNodeParameter.entityId = entityWithParentResultSet.getEntityId();
				createNodeParameter.entityType = entityWithParentResultSet.getEntityType();
				createNodeParameter.origin = entityWithParentResultSet.getEntityOrigin();

				inventoryGraphDelegate.createNodeForEntity(createNodeParameter, createNodeContext);
				createNodeContext.numberOfNodesCreated++;
			} catch (BusinessException bex) {
				createNodeContext.numberOfNodesCreationError++;
				LOGGER.warn(SOATools.buildSOALogMessage(soaContext,
						"Found multiple Entities with entityId=" + entityWithParentResultSet.getEntityId()
								+ " and origin=" + entityWithParentResultSet.getEntityOrigin()
								+ ". Only the first one have been inserted"));
			}
		}

		createNodeContext.transaction.success();
	}

	private void createParentEdges(EntityAndParentWithIdAndTypeResultSet entityWithParentResultSet,
			CreateEdgeContext createEdgeContext, SOAContext soaContext) throws SQLException {
		CreateEdgeParameter createEdgeParameter = new CreateEdgeParameter();

		createEdgeContext.numberOfEdgesCreated = 0;
		createEdgeContext.numberOfEdgesCreationError = 0;

		while (entityWithParentResultSet.next()) {
			try {
				if (entityWithParentResultSet.getEntityParentId() != null) {
					createEdgeParameter.destinationId = entityWithParentResultSet.getEntityId();
					createEdgeParameter.destinationType = entityWithParentResultSet.getEntityType();
					createEdgeParameter.destinationOrigin = entityWithParentResultSet.getEntityOrigin();

					createEdgeParameter.sourceId = entityWithParentResultSet.getEntityParentId();
					createEdgeParameter.sourceType = entityWithParentResultSet.getEntityParentType();
					createEdgeParameter.sourceOrigin = entityWithParentResultSet.getEntityParentOrigin();

					createEdgeParameter.parameter = "";
					createEdgeParameter.role = PHYSICAL_PARENT_ROLE;

					inventoryGraphDelegate.createEdge(createEdgeParameter, createEdgeContext, soaContext);
					createEdgeParameter.clear();
					createEdgeContext.numberOfEdgesCreated++;
				}
			} catch (Exception ex) {
				LOGGER.error(SOATools.buildSOALogMessage(soaContext,
						"Cannot create relathionship between " + entityWithParentResultSet.getEntityId() + " and "
								+ entityWithParentResultSet.getEntityParentId()),
						ex);
				createEdgeContext.numberOfEdgesCreationError++;
				if (ex.getCause() != null && ex.getCause() instanceof TransactionFailureException) {
					throw (TransactionFailureException) ex.getCause();
				} else if (ex.getCause() != null && ex.getCause().getCause() != null
						&& ex.getCause().getCause() instanceof TransactionFailureException) {
					throw (TransactionFailureException) ex.getCause().getCause();
				}
			}
		}

		createEdgeContext.transaction.success();
	}

	public void createComplexLinkEdges(LinkWithExtremityIdAndOriginResultSet linkWithExtremityIdAndOriginResultSet,
			CreateEdgeContext createEdgeContext, SOAContext soaContext) throws SQLException {
		createEdgeContext.numberOfEdgesCreated = 0;
		createEdgeContext.numberOfEdgesCreationError = 0;

		CreateEdgeParameter createEdgeParameter = new CreateEdgeParameter();

		while (linkWithExtremityIdAndOriginResultSet.next()) {
			try {
				createEdgeParameter.destinationId = linkWithExtremityIdAndOriginResultSet.getDestinationId();
				createEdgeParameter.destinationType = linkWithExtremityIdAndOriginResultSet.getDestinationType();
				createEdgeParameter.destinationOrigin = linkWithExtremityIdAndOriginResultSet.getDestinationOrigin();
				createEdgeParameter.sourceId = linkWithExtremityIdAndOriginResultSet.getSourceId();
				createEdgeParameter.sourceType = linkWithExtremityIdAndOriginResultSet.getSourceType();
				createEdgeParameter.sourceOrigin = linkWithExtremityIdAndOriginResultSet.getSourceOrigin();

				createEdgeParameter.parameter = linkWithExtremityIdAndOriginResultSet.getParameter();
				createEdgeParameter.role = linkWithExtremityIdAndOriginResultSet.getRole();
				inventoryGraphDelegate.createEdge(createEdgeParameter, createEdgeContext, soaContext);
				createEdgeContext.numberOfEdgesCreated++;
			} catch (Exception ex) {
				createEdgeContext.numberOfEdgesCreationError++;
				LOGGER.error(SOATools.buildSOALogMessage(soaContext,
						"Cannot create relathionship between " + linkWithExtremityIdAndOriginResultSet.getSourceId()
								+ " and " + linkWithExtremityIdAndOriginResultSet.getDestinationId() + " with role ="
								+ linkWithExtremityIdAndOriginResultSet.getRole() + " and parameter = "
								+ linkWithExtremityIdAndOriginResultSet.getParameter()),
						ex);
				if (ex.getCause() != null && ex.getCause() instanceof TransactionFailureException) {
					throw (TransactionFailureException) ex.getCause();
				} else if (ex.getCause() != null && ex.getCause().getCause() != null
						&& ex.getCause().getCause() instanceof TransactionFailureException) {
					throw (TransactionFailureException) ex.getCause().getCause();
				}
			}
		}

		createEdgeContext.transaction.success();
	}

	public GetGraphDatabaseListTO getGraphDatabases() {
		File graphDatabaseFolder = new File(InventoryGraphDelegate.getGraphDatabaseFolder());

		GetGraphDatabaseListTO getGraphDatabaseListTO = new GetGraphDatabaseListTO();
		File[] graphDatabasesFiles = graphDatabaseFolder.listFiles();

		for (int i = 0; i < graphDatabasesFiles.length; i++) {
			getGraphDatabaseListTO.databaseAbsolutePath.add(graphDatabasesFiles[i].getAbsolutePath());
		}

		return getGraphDatabaseListTO;
	}

	public void createCurrentGraphServiceFromExistingDatabase(String databaseLocation, SOAContext context)
			throws BusinessException, IOException {
		graphServerRegistry.createCurrentDatabaseGraphFromExistingFolder(databaseLocation,
				InventoryGraphDelegate.getGraphDatabaseIndexes(), context);
	}

	public ResultEntityNodeTO getHierarchyForEntityByLevel(GetHierarchyForEntityByLevelParameter parameter,
			SOAContext soaContext) throws BusinessException {
		Long methodStartTime = Utils.getTime();

		GraphDatabaseServiceWithStateProxy graphService = graphServerRegistry.getCurrentService();
		if (graphService == null) {
			throw new BusinessException(
					BusinessException.NO_GRAPH_LOADED_EXCEPTION + " : "
							+ BusinessException.NO_GRAPH_LOADED_EXCEPTION_MESSAGE,
					BusinessException.NO_GRAPH_LOADED_EXCEPTION);
		}
		Node node = graphService.getUniqueNodeByHierarchyForEntity(InventoryGraphDelegate.ENTITYID_PROPERTY, parameter);

		try {
			if (node == null) {
				graphService.release(soaContext);
				throw new BusinessException(
						BusinessException.ENTITY_NOT_FOUND_EXCEPTION_CODE + " : node with entityAndOrigin"
								+ parameter.entityId + " in currentGraph " + graphService,
						BusinessException.ENTITY_NOT_UNIQUE_EXCEPTION_CODE);
			}

			List<TraversalDescription> traversals = inventoryTraversalBuilder
					.buildTraversalForEntityBasedOnHierarchy(parameter, soaContext);

			if (LOGGER.isDebugEnabled()) {
				for (TraversalDescription traversalDescription : traversals) {
					String output = "";
					for (Path path : traversalDescription.traverse(node)) {
						output += "At depth " + path.length() + " => "
								+ path.startNode().getProperty(InventoryGraphDelegate.ENTITYTYPE_PROPERTY) + ", "
								+ path.startNode().getProperty(InventoryGraphDelegate.ENTITYID_PROPERTY) + " == "
								+ path.startNode().getId() + "\n" + " => "
								+ path.endNode().getProperty(InventoryGraphDelegate.ENTITYTYPE_PROPERTY) + ", "
								+ path.endNode().getProperty(InventoryGraphDelegate.ENTITYID_PROPERTY) + " == "
								+ path.endNode().getId() + "\n";

					}
					LOGGER.debug(SOATools.buildSOALogMessage(soaContext, output));
				}
			}

			List<Traverser> traversers = new ArrayList<Traverser>();
			for (TraversalDescription traversalDescription : traversals) {
				traversers.add(traversalDescription.traverse(node));
			}

			ResultEntityNodeTO resultRootNode = null;

			if (parameter.resultAsTree) {
				Map<Integer, Map<String, ResultEntityNodeTO>> resultsHierarchy = traversalResultFromPathBuilder
						.buildHierarchyFromPathesTraversal(traversers, soaContext);
				Map<String, ResultEntityNodeTO> levelZero = resultsHierarchy.get(0);
				resultRootNode = levelZero.get(0);
			} else {
				Set<ResultEntityNodeTO> resultRootNodes = traversalResultFromPathBuilder
						.buildGraphFromPathesTraversalAndReturnRootNodes(traversers, soaContext);
				resultRootNode = resultRootNodes.iterator().next();

				Set<String> rootNodesIds = new HashSet<String>();
				rootNodesIds.add(parameter.entityId);
			}

			Long methodEndTime = Utils.getTime();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "Result is " + resultRootNode));
			}

			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"getHierarchyForEntityByLevel executed in " + (methodEndTime - methodStartTime) + " ms"));

			graphService.release(soaContext);
			return resultRootNode;
		} catch (BusinessException rex) {
			graphService.release(soaContext);
			throw rex;
		} catch (RuntimeException rex) {
			graphService.release(soaContext);
			throw rex;
		}
	}

	public Date activeGraphDatabaseCreationDate() throws ParseException {
		Date creationDate;

		GraphDatabaseServiceWithStateProxy graphService = graphServerRegistry.getCurrentService();

		if (graphService == null) {
			creationDate = null;
		} else {
			DateFormat df = new SimpleDateFormat(InventoryGraphDelegate.DB_DATE_FORMATTER.getPattern());
			creationDate = df.parse(
					StringUtils.removeStart(graphService.getDirectory().getName(), InventoryGraphDelegate.DB_NAME));
		}

		return creationDate;
	}

	/**
	 * {@link GraphServerRegistry#flushAllExceptActiveGraphDatabases(SOAContext)}
	 * 
	 * @param soaContext
	 * @return the number of Neo4J graph databases that have been flushed from memory
	 * 
	 * @author Pascal Morvan (Atos)
	 * @see [GSD02314-2018-114206-Openstat-BaseGraphNeo4J--ProgrammerLeMenage, PR_02314_P228_003]
	 */
	public Integer flushAllExceptActiveGraphDatabases(SOAContext soaContext) {
		return graphServerRegistry.flushAllExceptActiveGraphDatabases(soaContext);
	}

}

package com.orange.srs.refreport.provider.service.rest.inventory;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA16InventoryGraphFacade;
import com.orange.srs.refreport.model.TO.inventory.GraphDatabaseServiceTOList;
import com.orange.srs.refreport.model.enumerate.InventoryGraphEdgeOrientationEnum;
import com.orange.srs.refreport.model.enumerate.InventoryGraphEdgeTypeEnum;
import com.orange.srs.refreport.model.parameter.inventory.GraphFolderParameter;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refreport.technical.graph.GraphDatabaseServiceWithStateProxy;
import com.orange.srs.refreport.technical.graph.GraphServerRegistry;
import com.orange.srs.statcommon.model.TO.inventory.ResultEntityNodeTO;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.inventory.EdgeToTraverseDefinitionParameter;
import com.orange.srs.statcommon.model.parameter.inventory.GetHierarchyForEntityByLevelParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

@Stateless
@Path("inventoryGraph")
public class GraphService {

	private static final Logger LOGGER = Logger.getLogger(GraphService.class);

	@Context
	private UriInfo context;

	@EJB
	private GraphServerRegistry graphServerRegistry;

	@EJB
	private SOA16InventoryGraphFacade soa16InventoryGraphFacade;

	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@Path("/administration/graphs")
	public GraphDatabaseServiceTOList getGraphs() {
		return graphServerRegistry.getGraphServicesList();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@Path("/administration/current/inmemory")
	public Response putCurrentGraphInMemory() {
		SOAContext soaContext = SOATools.buildSOAContext(null);
		try {
			graphServerRegistry.loadCurrentGraphInMemory(soaContext);
		} catch (Exception ex) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, ex.getMessage()), ex);
			return Response.serverError().build();
		}

		return Response.ok().build();
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@Path("/administration/graphs")
	public Response deleteAllGraphs() {
		try {
			graphServerRegistry.purgeRegistry();
		} catch (Exception bex) {
			LOGGER.error(bex.getMessage(), bex);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();

		}
		return Response.ok().build();
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@Path("/administration/graph/")
	public Response deleteGraph(GraphFolderParameter graphFolderParameter) {
		SOAContext soaContext = SOATools.buildSOAContext(graphFolderParameter);

		try {
			graphServerRegistry.forceGraphServicePurge(graphFolderParameter.graphFolder, soaContext);
		} catch (BusinessException bex) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, bex.getMessage()), bex);
			return Response.status(Status.BAD_REQUEST).build();
		}

		return Response.ok().build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response createNewInventoryGraph() {
		SOAContext soaContext = SOATools.buildSOAContext(null);
		URI location = context.getAbsolutePath();
		try {
			soa16InventoryGraphFacade.createAndActivateInventoryGraphAsynchronously(new Date(), soaContext);
		} catch (Exception ex) {
			ex.printStackTrace();
			LOGGER.error(ex.getMessage(), ex);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}

		return Response.created(location).build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@Path("/administration/graph")
	public Response createEmptyGraph(GraphFolderParameter parameter) {
		SOAContext soaContext = SOATools.buildSOAContext(parameter);
		URI location = context.getAbsolutePath();
		try {
			// String[] indexes = {};
			String[] indexes = null;
			GraphDatabaseServiceWithStateProxy proxy = graphServerRegistry
					.createGraphDatabaseService(parameter.graphFolder, indexes, soaContext);
			proxy.activate(soaContext);
		} catch (BusinessException bex) {
			bex.printStackTrace();
			LOGGER.error(bex.getMessage(), bex);
			return Response.status(Status.BAD_REQUEST).build();
		} catch (IOException ioex) {
			ioex.printStackTrace();
			LOGGER.error(ioex.getMessage(), ioex);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}

		return Response.created(location).build();
	}

	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@Path("/administration/graph/client/add/{timer}")
	public Response addClient(@PathParam("timer") Long timer) {
		SOAContext context = SOATools.buildSOAContext(null);
		GraphDatabaseServiceWithStateProxy proxy = graphServerRegistry.getCurrentService();
		try {
			Thread.sleep(timer);
		} catch (Exception e) {

		} finally {
			try {
				proxy.release(context);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return Response.ok().build();
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/administration/existing")
	public Response getExistingBase() {
		Response response = null;
		if (soa16InventoryGraphFacade.testGraphDatabaseConnection()) {
			response = Response.ok().entity(soa16InventoryGraphFacade.getGraphDatabases()).build();
		} else {
			response = RestResponseFactory.makeExceptionResponseFactory("message", Response.Status.NOT_FOUND);
		}
		return response;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	@Path("/administration/graph/active")
	public Response createActiveGraphFromexistingFolder(GraphFolderParameter parameter) {
		SOAContext soaContext = SOATools.buildSOAContext(parameter);
		URI location = context.getAbsolutePath();
		try {
			soa16InventoryGraphFacade.createCurrentGraphServiceFromExistingDatabase(parameter.graphFolder, soaContext);
		} catch (BusinessException bex) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, bex.getMessage(), bex));
			return Response.status(Status.BAD_REQUEST).build();
		} catch (Exception ex) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, ex.getMessage(), ex));
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}

		return Response.created(location).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@Path("/hierarchy/entity")
	public Response getHierarchyByEntity(GetHierarchyForEntityByLevelParameter parameter) {
		SOAContext soaContext = SOATools.buildSOAContext(null);
		ResultEntityNodeTO getHierarchyForEntityByLevelTO = null;

		try {
			getHierarchyForEntityByLevelTO = soa16InventoryGraphFacade.getHierarchyForEntityByLevel(parameter,
					soaContext);
		} catch (BusinessException bex) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, bex.getMessage(), bex));
			return Response.status(Status.BAD_REQUEST).build();
		} catch (Exception ioex) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, ioex.getMessage(), ioex));
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}

		return Response.ok(getHierarchyForEntityByLevelTO).build();

	}

	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@Path("/hierarchyByEntityTest/{testid}")
	public Response getHierarchyByEntityTest(@PathParam("testid") int testid) {
		SOAContext soaContext = SOATools.buildSOAContext(null);
		ResultEntityNodeTO getHierarchyForEntityByLevelTO = null;
		GetHierarchyForEntityByLevelParameter parameter = new GetHierarchyForEntityByLevelParameter();

		if (testid == 0) {
			parameter.entityId = "ORIGIN:SCE.DI:AREVA.SITE:AREVA NC LA HAGUE";

			EdgeToTraverseDefinitionParameter param = new EdgeToTraverseDefinitionParameter();
			param.edgeOrientation = InventoryGraphEdgeOrientationEnum.OUTGOING.toString();
			param.edgeType = InventoryGraphEdgeTypeEnum.HAS_EQUIPMENT.toString();
			parameter.edgeToTraverseDefinitions.add(param);

			EdgeToTraverseDefinitionParameter param2 = new EdgeToTraverseDefinitionParameter();
			param2.edgeOrientation = InventoryGraphEdgeOrientationEnum.OUTGOING.toString();
			param2.edgeType = InventoryGraphEdgeTypeEnum.IS_PARENT.toString();
			param.getNextEdgeToTraverseDefinition().add(param2);

			EdgeToTraverseDefinitionParameter param3 = new EdgeToTraverseDefinitionParameter();
			param3.edgeOrientation = InventoryGraphEdgeOrientationEnum.OUTGOING.toString();
			param3.edgeType = InventoryGraphEdgeTypeEnum.IS_PARENT.toString();
			param2.getNextEdgeToTraverseDefinition().add(param3);
		} else {
			parameter.entityId = "ORIGIN:EQUANT.DI:100076.ZI:X.LIRT:PZLB010-CONTROLNET-763107468";

			EdgeToTraverseDefinitionParameter param = new EdgeToTraverseDefinitionParameter();
			param.edgeOrientation = InventoryGraphEdgeOrientationEnum.OUTGOING.toString();
			param.edgeType = InventoryGraphEdgeTypeEnum.HAS_PHYSICALCONNECTION_ORIGINFDN.toString();
			parameter.edgeToTraverseDefinitions.add(param);

			EdgeToTraverseDefinitionParameter param2 = new EdgeToTraverseDefinitionParameter();
			param2.edgeOrientation = InventoryGraphEdgeOrientationEnum.INCOMING.toString();
			param2.edgeType = InventoryGraphEdgeTypeEnum.IS_PARENT.toString();
			param.getNextEdgeToTraverseDefinition().add(param2);

			EdgeToTraverseDefinitionParameter param3 = new EdgeToTraverseDefinitionParameter();
			param3.edgeOrientation = InventoryGraphEdgeOrientationEnum.INCOMING.toString();
			param3.edgeType = InventoryGraphEdgeTypeEnum.HAS_EQUIPMENT.toString();
			param2.getNextEdgeToTraverseDefinition().add(param3);
		}

		try {
			getHierarchyForEntityByLevelTO = soa16InventoryGraphFacade.getHierarchyForEntityByLevel(parameter,
					soaContext);
		} catch (BusinessException bex) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, bex.getMessage(), bex));
			return Response.status(Status.BAD_REQUEST).build();
		} catch (Exception ioex) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, ioex.getMessage(), ioex));
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}

		return Response.ok(getHierarchyForEntityByLevelTO).build();
	}

	/**
	 * Flush all the Neo4J graph databases with state NEW/INACTIVE/TO_GARBAGE from memory.<br/>
	 * Keep the ACTIVE ones.<br/>
	 * Do not delete the Neo4J graph database files on disk.<br/>
	 * 
	 * @return Response containing the number of Neo4J graph databases that have been flushed from memory
	 * 
	 * @author Pascal Morvan (Atos)
	 * @see [GSD02314-2018-114206-Openstat-BaseGraphNeo4J--ProgrammerLeMenage, PR_02314_P228_001]
	 *      {@link GraphServerRegistry#flushAllExceptActiveGraphDatabases(SOAContext)}
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/administration/flushAllExceptActiveGraphs")
	public Response flushAllExceptActiveGraphs() {
		SOAContext soaContext = SOATools.buildSOAContext(null);
		try {
			String serviceName = "flushAllExceptActiveGraphs";
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, String.format("Start: %s service", serviceName)));

			Integer count = graphServerRegistry.flushAllExceptActiveGraphDatabases(soaContext);
			String result = String.format("%s=%d", "flushedGraphDatabaseCount", count);

			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					String.format("End: %s service ; %s", serviceName, result)));
			return Response.ok(result).build();
		} catch (Exception e) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, e.getMessage(), e));
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}

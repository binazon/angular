package com.orange.srs.refreport.provider.service.rest.inventory;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA01InventoryFacade;
import com.orange.srs.refreport.business.SOA03CatalogFacade;
import com.orange.srs.refreport.business.SOA04UserFacade;
import com.orange.srs.refreport.model.TO.ReportingGroupTOList;
import com.orange.srs.refreport.model.external.inventory.GetAmountOfReportingGroupTO;
import com.orange.srs.refreport.model.external.inventory.GetReportGroupAmountParameter;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.TO.GetReportingGroupsKeysTO;
import com.orange.srs.statcommon.model.TO.PerimetersByCredentialTOList;
import com.orange.srs.statcommon.model.TO.rest.GetEntitiesForTypeAndForReportingGroupTO;
import com.orange.srs.statcommon.model.enums.OfferOptionTypeEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

/**
 * Rest services to manipulate ReportingGroup
 * 
 * @author A159138
 */
@Stateless
@Path("reportingGroup")
public class ReportingGroupService {

	private static final Logger LOGGER = Logger.getLogger(ReportingGroupService.class);

	@Context
	private UriInfo context;

	@EJB
	private SOA01InventoryFacade inventoryFacade;

	@EJB
	private SOA04UserFacade userFacade;

	@EJB
	private SOA03CatalogFacade catalogFacade;

	/**
	 * DELETE method for Removing an instance of ReportingGroup
	 * 
	 * @return 200 0K
	 * @return 405 Bad Request : Body and/or url is wrong
	 */
	@DELETE
	@Path("{origin}/{reportingGroupRef}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response removeReportingGroup(@PathParam("origin") String origin,
			@PathParam("reportingGroupRef") String reportingGroupRef) {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "ReportingGroup - DELETE service call"));

			inventoryFacade.removeReportingGroupByOriginAndRef(origin, reportingGroupRef, soaContext);
			response = Response.ok().build();

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "ReportingGroup - DELETE response built"));
		} catch (BusinessException bex) {
			response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.BAD_REQUEST);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}

	/**
	 * Get all reporting group keys
	 * 
	 * @return 200 0K : ReportingGroup keys
	 */
	@GET
	@Path("keys")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getReportingGroupKeys() {
		Response response = null;

		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "ReportingGroup - keys service call"));

			GetReportingGroupsKeysTO result = inventoryFacade.getReportingGroupKeys(soaContext);
			response = Response.ok(result).build();

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "ReportingGroup - keys response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}

		return response;
	}

	/**
	 * GET method of all instances of ReportingGroup
	 * 
	 * @return 200 0K : All ReportingGroups in body
	 */
	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getAllReportingGroups() {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "ReportingGroup - GET service call"));

			ReportingGroupTOList list = inventoryFacade.getAllReportingGroups(soaContext);
			response = Response.ok(list).build();

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "ReportingGroup - GET response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}

	/**
	 * Get all reporting group keys for OpenDashboard inventory files retrieving
	 * 
	 * @return 200 0K : ReportingGroup keys
	 */
	@GET
	@Path("interactiveKeys")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getInteractiveReportingGroupKeys() {
		Response response = null;

		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Interactive ReportingGroup - keys service call"));

			GetReportingGroupsKeysTO result = inventoryFacade.getInteractiveReportingGroupKeys(soaContext);
			response = Response.ok(result).build();

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Interactive ReportingGroup - keys response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}

		return response;
	}

	/**
	 * Get all reporting group keys
	 * 
	 * @return 200 0K : ReportingGroup keys
	 */
	@POST
	@Path("amount")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public Response getReportingGroupAmount(GetReportGroupAmountParameter parameter) {
		Response response = null;

		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			LOGGER.info(SOATools.buildSOAStartLogMessage(soaContext, "getReportingGroupAmount", parameter.toString()));

			int result = inventoryFacade.getReportingGroupAmount(parameter, soaContext);
			GetAmountOfReportingGroupTO to = new GetAmountOfReportingGroupTO();
			to.amountOfReportingGroup = result;
			response = Response.ok(to).build();

			LOGGER.info(SOATools.buildSOAEndLogMessage(soaContext, "getReportingGroupAmount", parameter.toString()));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}

		return response;
	}

	/*
	 * @GET
	 * 
	 * @Path("amount")
	 * 
	 * @Produces(MediaType.APPLICATION_XML) public Response gettReportingGroupAmount() { Response response = null;
	 * 
	 * 
	 * try { // build SOA Context for logging SOAContext soaContext = SOATools.buildSOAContext( null);
	 * 
	 * logger.info(SOATools.buildSOALogMessage( soaContext,"ReportingGroup - amount service call"));
	 * 
	 * //int result = inventoryFacade.getReportingGroupAmount(parameter, soaContext); GetAmountOfReportingGroupTO to=new
	 * GetAmountOfReportingGroupTO(); to.amountOfReportingGroup=1; response = Response.ok(to).build();
	 * 
	 * logger.info(SOATools.buildSOALogMessage( soaContext,"ReportingGroup - amount response built")); } catch
	 * (RuntimeException ejbtex) { response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger); }
	 * 
	 * return response; }
	 */

	/**
	 * GET method to retrieve all the entities belonging to the reportingGroup PassedInParamer, and of the paramType
	 * Specified in parameter If startWith query parameter is not empty, then retrieved entities will be filtered
	 * accordingly
	 */
	@GET
	@Path("/{reportingGroupRef}/origin/{origin}/entityType/{typeAlias}/entity")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEntitiesForTypeAndForReportingGroup(@PathParam("reportingGroupRef") String reportingGroupRef,
			@PathParam("origin") String origin, @PathParam("typeAlias") String entityTypeAlias,
			@DefaultValue("") @QueryParam("startWith") String entityLabelStartWith) {
		Response response = null;

		// build SOA Context for logging
		SOAContext soaContext = SOATools.buildSOAContext(null);
		try {
			if ("".equals(entityLabelStartWith))
				entityLabelStartWith = null;

			GetEntitiesForTypeAndForReportingGroupTO result = userFacade.getEntitiesForTypeAndForReportingGroup(
					reportingGroupRef, origin, entityTypeAlias, entityLabelStartWith, soaContext);
			response = Response.ok(result).build();
		} catch (BusinessException e) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, "Error retreiving entities for reportingGroup "
					+ reportingGroupRef + "/" + origin + " with entityType " + entityTypeAlias), e);
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.BAD_REQUEST);
		}
		return response;
	}

	@GET
	@Path("/{reportingGroupRef}/origin/{origin}/entityType/{typeAlias}/entity/cassandra")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEntitiesForTypeAndForReportingGroupForCassandra(
			@PathParam("reportingGroupRef") String reportingGroupRef, @PathParam("origin") String origin,
			@PathParam("typeAlias") String entityTypeAlias) {
		Response response = null;

		// build SOA Context for logging
		SOAContext soaContext = SOATools.buildSOAContext(null);
		try {

			GetEntitiesForTypeAndForReportingGroupTO result = userFacade
					.getEntitiesForTypeAndForReportingGroupForCassandra(reportingGroupRef, origin, entityTypeAlias,
							soaContext);
			response = Response.ok(result).build();
		} catch (BusinessException e) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, "Error retreiving entities for reportingGroup "
					+ reportingGroupRef + "/" + origin + " with entityType " + entityTypeAlias), e);
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.BAD_REQUEST);
		}
		return response;
	}

	@GET
	@Path("{origin}/offerOption")
	@Produces(MediaType.APPLICATION_XML)
	public Response getReportingGroupsByOfferOption(@PathParam("origin") String origin,
			@QueryParam("optionAlias") List<String> optionAliases) {

		Response response = null;
		SOAContext soaContext = SOATools.buildSOAContext(null);
		try {
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "getReportingGroupsByOfferOption service call"));

			PerimetersByCredentialTOList list = catalogFacade.getReportingGroupKeysByOfferOption(origin, optionAliases);
			response = Response.ok(list).build();

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "getReportingGroupsByOfferOption response built"));
		} catch (BusinessException bex) {
			if (bex.code == BusinessException.ENTITY_NOT_FOUND_EXCEPTION_CODE) {
				LOGGER.warn(SOATools.buildSOALogMessage(soaContext, bex.getMessage()));
				response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.EXPECTATION_FAILED);
			} else if (bex.code == BusinessException.NO_DATA_FOUND_EXCEPTION) {
				LOGGER.warn(SOATools.buildSOALogMessage(soaContext, bex.getMessage()));
				response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.NO_CONTENT);
			} else {
				response = RestResponseFactory.makeInternalErrorResponseFactory(bex, LOGGER);
			}
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}

	@GET
	@Path("exportToFile")
	@Produces(MediaType.APPLICATION_XML)
	public Response getReportingGroupsByOfferOptionType(
			@QueryParam("offerOptionType") List<OfferOptionTypeEnum> offerOptionTypes) {

		Response response = null;
		SOAContext soaContext = SOATools.buildSOAContext(null);
		try {
			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"getReportingGroupsByOriginAndOfferOptionType service call"));

			String generatedFilePath = inventoryFacade.getReportingGroupsByOfferOptionType(offerOptionTypes,
					soaContext);
			response = Response.ok(generatedFilePath).build();

			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"getReportingGroupsByOriginAndOfferOptionType response built"));
		} catch (BusinessException bex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(bex, LOGGER);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}
}

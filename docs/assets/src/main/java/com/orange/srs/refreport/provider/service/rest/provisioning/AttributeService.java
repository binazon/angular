package com.orange.srs.refreport.provider.service.rest.provisioning;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.SOA17AttributFacade;
import com.orange.srs.refreport.model.parameter.EntityToEntityAttributeParameterList;
import com.orange.srs.refreport.model.parameter.GroupEntityAttributeParameterList;
import com.orange.srs.statcommon.model.parameter.GroupAttributeParameterList;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

@Stateless
@Path("attributes")
public class AttributeService {

	private static Logger LOGGER = Logger.getLogger(AttributeService.class);

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private SOA17AttributFacade attributFacade;

	@GET
	@Path("group")
	@Produces(MediaType.APPLICATION_XML)
	public Response getAllGroupAttribute() {
		Response response = null;
		try {
			// Build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "GroupEntityAttribute - GET service call"));
			GroupAttributeParameterList groupAttributeParameters = attributFacade.getGroupAttributes(soaContext);
			response = Response.ok(groupAttributeParameters).build();
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "GroupEntityAttribute - GET response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}

	@GET
	@Path("group/{origin}/{reportingGroupRef}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getGroupAttribute(@PathParam("origin") String origin,
			@PathParam("reportingGroupRef") String reportingGroupRef) {
		Response response = null;
		try {
			// Build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "GroupEntityAttribute - GET service call"));
			GroupAttributeParameterList groupAttributeParameters = attributFacade.getGroupAttributes(origin,
					reportingGroupRef, soaContext);
			response = Response.ok(groupAttributeParameters).build();
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "GroupEntityAttribute - GET response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}

	@GET
	@Path("groupEntity")
	@Produces(MediaType.APPLICATION_XML)
	public Response getAllGroupEntityAttribute() {
		Response response = null;
		try {
			// Build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "GroupEntityAttribute - GET service call"));
			GroupEntityAttributeParameterList groupEntityAttributeParameters = attributFacade
					.getGroupEntityAttributes(soaContext);
			response = Response.ok(groupEntityAttributeParameters).build();
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "GroupEntityAttribute - GET response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}

	@GET
	@Path("groupEntity/{origin}/{reportingGroupRef}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getGroupEntityAttribute(@PathParam("origin") String origin,
			@PathParam("reportingGroupRef") String reportingGroupRef) {
		Response response = null;
		try {
			// Build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "GroupEntityAttribute - GET service call"));
			GroupEntityAttributeParameterList groupEntityAttributeParameters = attributFacade
					.getGroupEntityAttributes(origin, reportingGroupRef, soaContext);
			response = Response.ok(groupEntityAttributeParameters).build();
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "GroupEntityAttribute - GET response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}

	@GET
	@Path("groupEntity/{entityId}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getGroupEntityAttributeByEntity(@PathParam("entityId") String entityId) {
		Response response = null;
		try {
			// Build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "getGroupEntityAttributeByEntity - GET service call"));
			GroupEntityAttributeParameterList groupEntityAttributeParameters = attributFacade
					.getGroupEntityAttributesByEntity(entityId, soaContext);
			response = Response.ok(groupEntityAttributeParameters).build();
			LOGGER.info(
					SOATools.buildSOALogMessage(soaContext, "getGroupEntityAttributeByEntity - GET response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}

	@GET
	@Path("groupEntity/{origin}/{reportingGroupRef}/{entityId}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getGroupEntityAttributeByGroupAndEntity(@PathParam("origin") String origin,
			@PathParam("reportingGroupRef") String reportingGroupRef, @PathParam("entityId") String entityId) {
		Response response = null;
		try {
			// Build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "getGroupEntityAttributeByEntity - GET service call"));
			GroupEntityAttributeParameterList groupEntityAttributeParameters = attributFacade
					.getGroupEntityAttributesByGroupAndEntity(origin, reportingGroupRef, entityId, soaContext);
			response = Response.ok(groupEntityAttributeParameters).build();
			LOGGER.info(
					SOATools.buildSOALogMessage(soaContext, "getGroupEntityAttributeByEntity - GET response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}

	@GET
	@Path("entityToEntity")
	@Produces(MediaType.APPLICATION_XML)
	public Response getAllEntityToEntityAttribute() {
		Response response = null;
		try {
			// Build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "EntityToEntityAttribute - GET service call"));
			EntityToEntityAttributeParameterList entityToEntityAttributeParameters = attributFacade
					.getEntityToEntityAttributes(soaContext);
			response = Response.ok(entityToEntityAttributeParameters).build();
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "EntityToEntityAttribute - GET response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}

	@GET
	@Path("entityToEntity/source/{entityId}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEntityToEntityAttributeByEntitySource(@PathParam("entityId") String entityId) {
		Response response = null;
		try {
			// Build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);
			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"getEntityToEntityAttributeByEntitySource - GET service call"));
			EntityToEntityAttributeParameterList entityToEntityAttributeParameters = attributFacade
					.getEntityToEntityAttributesByEntitySource(entityId, soaContext);
			response = Response.ok(entityToEntityAttributeParameters).build();
			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"getEntityToEntityAttributeByEntitySource - GET response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}

	@GET
	@Path("entityToEntity/end/{entityId}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEntityToEntityAttributeByEntityDest(@PathParam("entityId") String entityId) {
		Response response = null;
		try {
			// Build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);
			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"getEntityToEntityAttributesByEntityDest - GET service call"));
			EntityToEntityAttributeParameterList entityToEntityAttributeParameters = attributFacade
					.getEntityToEntityAttributesByEntityDest(entityId, soaContext);
			response = Response.ok(entityToEntityAttributeParameters).build();
			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"getEntityToEntityAttributesByEntityDest - GET response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}
}

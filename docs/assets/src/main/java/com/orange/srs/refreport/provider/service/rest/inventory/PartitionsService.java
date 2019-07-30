package com.orange.srs.refreport.provider.service.rest.inventory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA15PartitioningFacade;
import com.orange.srs.statcommon.model.TO.ReportingGroupPartitionStatusTOList;
import com.orange.srs.statcommon.model.TO.rest.PartitionStatusParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

@Stateless
@Path("partitions")
public class PartitionsService {

	private static final Logger LOGGER = Logger.getLogger(PartitionsService.class);

	@EJB
	private SOA15PartitioningFacade partitioningFacade;

	@POST
	@Path("entities/export")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response exportEntitiesPartitions() {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "ExportEntitiesPartitions - POST service call"));

			partitioningFacade.exportEntitiesPartitions(soaContext);
			response = Response.ok().build();

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "ExportEntitiesPartitions - POST response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}

	@PUT
	@Path("entities/import")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response importEntitiesPartitions() {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "ImportEntitiesPartitions - PUT service call"));

			partitioningFacade.importEntitiesPartitions(soaContext);
			response = Response.ok().build();

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "ImportEntitiesPartitions - PUT response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}

	@POST
	@Path("entities/update")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response updateEntitiesPartitions() {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "UpdateEntitiesPartitions - POST service call"));

			partitioningFacade.updateEntitiesPartitions(soaContext);

			response = Response.ok().build();

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "UpdateEntitiesPartitions - POST response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}

	@POST
	@Path("status/update")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response updatePartitionsStatus() {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "UpdateEntitiesPartitions - POST service call"));

			partitioningFacade.updatePartitionsStatus(soaContext);

			response = Response.ok().build();

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "UpdateEntitiesPartitions - POST response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}

	@POST
	@Path("groups/update")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response updateGroupPartitions() {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "UpdateGroupPartitions - POST service call"));

			partitioningFacade.updateGroupsPartitions(soaContext);
			response = Response.ok().build();

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "UpdateGroupPartitions - POST response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}

	@POST
	@Path("groupsForEntityType")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getGroupPartitionsForEntityType(PartitionStatusParameter getPartitionStatus) {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "getGroupPartitionsForEntityType - POST service call"));

			ReportingGroupPartitionStatusTOList groupPartitionStatus = partitioningFacade
					.getGroupsPartitions(getPartitionStatus, soaContext);
			response = Response.ok(groupPartitionStatus).build();

			LOGGER.info(
					SOATools.buildSOALogMessage(soaContext, "getGroupPartitionsForEntityType - POST response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}
}
package com.orange.srs.refreport.provider.service.rest.catalog;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA01InventoryFacade;
import com.orange.srs.statcommon.model.TO.FilterTOList;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

@Stateless
@Path("filter")
public class FilterService {

	private static final Logger LOGGER = Logger.getLogger(FilterService.class);

	@EJB
	private SOA01InventoryFacade inventoryFacade;

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getAllFilters() {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "Filter - GET service call"));

			FilterTOList list = inventoryFacade.getAllFilters(soaContext);
			response = Response.ok(list).build();

			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "Filter - GET response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}

	@GET
	@Path("export")
	@Produces(MediaType.APPLICATION_XML)
	public Response exportFilters() {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "Filter - GET service call"));

			inventoryFacade.exportAllFilters(soaContext);

			response = Response.ok().build();

			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "Filter - GET response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}
}

package com.orange.srs.refreport.provider.service.rest.inventory;

import java.io.IOException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA01InventoryFacade;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

/**
 * Rest services to get inventories files
 * 
 * @author A128239
 */
@Stateless
@Path("purgeInventoryFile")
public class PurgeInventoryFileService {

	private static final Logger LOGGER = Logger.getLogger(PurgeInventoryFileService.class);

	@EJB
	private SOA01InventoryFacade inventoryFacade;

	/**
	 * Retrieves the inventory report template file to return
	 * 
	 */
	@GET
	@Path("db")
	@Produces(MediaType.APPLICATION_XML)
	public Response getInventoryReportTemplateFile() {
		// build SOA Context for logging
		SOAContext soaContext = SOATools.buildSOAContext(null);

		Response response = null;
		try {

			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "Purge Inventory File service call"));

			inventoryFacade.purgeInventoryFile();
			response = Response.ok().type(MediaType.APPLICATION_XML).build();

			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "Purge Inventory File response built"));
		} catch (IOException e) {
			response = RestResponseFactory.makeInternalErrorResponseFactoryWithXmlType(e, LOGGER);
		} catch (RuntimeException e) {
			response = RestResponseFactory.makeInternalErrorResponseFactoryWithXmlType(e, LOGGER);
		}
		return response;
	}

}
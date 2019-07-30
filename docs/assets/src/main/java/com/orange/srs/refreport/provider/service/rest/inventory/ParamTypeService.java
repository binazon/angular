package com.orange.srs.refreport.provider.service.rest.inventory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA01InventoryFacade;
import com.orange.srs.refreport.model.ParamType;
import com.orange.srs.statcommon.model.TO.ParamTypeTOList;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

/**
 * Rest services to manipulate {@link ParamType}
 * 
 * @author A128239
 */
@Stateless
@Path("paramType")
public class ParamTypeService {

	@EJB
	private SOA01InventoryFacade inventoryFacade;

	private static final Logger LOGGER = Logger.getLogger(ParamTypeService.class);

	/**
	 * GET method of all instances of {@link ParamType}
	 * 
	 * @return {@link Status#OK} : All {@link ParamType}s in body
	 */
	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getAllParamTypes() {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "ParamType - GET service call"));

			ParamTypeTOList list = inventoryFacade.getAllParamTypes(soaContext);
			response = Response.ok(list).build();

			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "ParamType - GET response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}

}

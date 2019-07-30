package com.orange.srs.refreport.provider.service.rest.report;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA02ReportFacade;
import com.orange.srs.statcommon.model.TO.GetInputFormatFullTO;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

@Stateless
@Path("inputFormat")
public class InputFormatService {

	private static Logger logger = Logger.getLogger(InputFormatService.class);

	@EJB
	private SOA02ReportFacade reportFacade;

	/**
	 * Retrieves representation of an instance of InventoryService
	 * 
	 * @return an instance of String
	 */
	@GET
	@Path("{formatType}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getInputFormat(@PathParam("formatType") String formatType) {

		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.info(SOATools.buildSOALogMessage(soaContext, "InputFormat - GET service call"));

			GetInputFormatFullTO result = reportFacade.getInputFormat(formatType);
			response = Response.status(Status.OK).entity(result).build();

			logger.info(SOATools.buildSOALogMessage(soaContext, "InputFormat - GET response built"));
		} catch (RuntimeException e) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(e, logger);
		}

		return response;
	}
}

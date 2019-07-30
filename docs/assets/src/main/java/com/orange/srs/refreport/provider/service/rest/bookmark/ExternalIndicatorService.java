package com.orange.srs.refreport.provider.service.rest.bookmark;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA02ReportFacade;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.TO.rest.ExternalIndicatorTO;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

@Stateless
@Path("externalIndicator")
public class ExternalIndicatorService {

	@EJB
	private SOA02ReportFacade reportFacade;

	private static final Logger LOGGER = Logger.getLogger(ExternalIndicatorService.class);

	@GET
	@Path("{label}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getExternalIndicator(@PathParam("label") String label) {

		Response response;
		SOAContext soaContext = SOATools.buildSOAContext(null);
		try {
			ExternalIndicatorTO externalIndicatorTO = reportFacade.getExternalIndicator(soaContext, label);
			response = Response.ok(externalIndicatorTO).build();
		} catch (BusinessException e) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, "Error retreiving externak indicator " + label, e));
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.BAD_REQUEST);
		} catch (Exception e) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, "Error retreiving externak indicator " + label, e));
			return RestResponseFactory.makeInternalErrorResponseFactory(e, LOGGER);
		}
		return response;
	}
}

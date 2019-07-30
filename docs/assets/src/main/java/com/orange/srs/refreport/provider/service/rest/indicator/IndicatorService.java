package com.orange.srs.refreport.provider.service.rest.indicator;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA04UserFacade;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.TO.rest.GetOfferOptionByIndicatorTO;
import com.orange.srs.statcommon.model.TO.rest.GetTypesForReportTO;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

@Stateless
@Path("indicator")
public class IndicatorService {

	@EJB
	private SOA04UserFacade userFacade;

	@Context
	private UriInfo context;

	private static Logger logger = Logger.getLogger(IndicatorService.class);

	@GET
	@Path("/{indicatorId}/group/{reportingGroupRef}/origin/{origin}/offer")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getxml(@PathParam("indicatorId") String indicatorId,
			@PathParam("reportingGroupRef") String reportingGroupRef, @PathParam("origin") String origin) {
		SOAContext context = SOATools.buildSOAContext(null);
		Response response;
		try {
			GetOfferOptionByIndicatorTO result = userFacade.getOfferByIndicatorForGroup(indicatorId, reportingGroupRef,
					origin, context);
			response = Response.ok(result).build();
		} catch (BusinessException e) {
			logger.error(SOATools.buildSOALogMessage(context,
					"Error retreiving offerOptions including indicator " + indicatorId), e);
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.BAD_REQUEST);
		}
		return response;
	}

	@GET
	@Path("/{indicatorId}/offer/{offerOptionAlias}/types")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response get2xml(@PathParam("indicatorId") String indicatorId,
			@PathParam("offerOptionAlias") String offerAlias) {

		SOAContext context = SOATools.buildSOAContext(null);
		Response response;
		try {
			GetTypesForReportTO result = userFacade.getTypesForReport(indicatorId, offerAlias, context);
			response = Response.ok(result).build();
		} catch (BusinessException e) {
			logger.error(SOATools.buildSOALogMessage(context, "Error retreiving types for indicator " + indicatorId)
					+ " and for offer " + offerAlias, e);
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.BAD_REQUEST);
		}
		return response;
	}
}

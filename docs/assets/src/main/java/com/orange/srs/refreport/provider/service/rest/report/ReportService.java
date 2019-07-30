package com.orange.srs.refreport.provider.service.rest.report;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA02ReportFacade;
import com.orange.srs.statcommon.model.TO.GetPatternTOList;
import com.orange.srs.statcommon.model.TO.GetReportsAndOfferOptionTO;
import com.orange.srs.statcommon.model.TO.GetReportsTO;
import com.orange.srs.statcommon.model.parameter.GetPatternParameterList;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

@Stateless
@Path("report")
public class ReportService {

	private static Logger logger = Logger.getLogger(ReportService.class);

	@EJB
	private SOA02ReportFacade reportFacade;

	/**
	 * Default constructor.
	 */
	public ReportService() {
	}

	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getReport() {

		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.info(SOATools.buildSOALogMessage(soaContext, "Report - GET service call"));

			GetReportsTO result = reportFacade.getReports();
			response = Response.status(Status.OK).entity(result).build();

			logger.info(SOATools.buildSOALogMessage(soaContext, "Report - GET response built"));
		} catch (RuntimeException e) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(e, logger);
		}

		return response;
	}

	@POST
	@Path("patterns")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getPattern(GetPatternParameterList parameter) {

		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(parameter);

			logger.debug(SOATools.buildSOAStartLogMessage(soaContext, "getPattern", parameter.toString()));

			GetPatternTOList resultList = new GetPatternTOList();
			resultList = reportFacade.getPatterns(parameter);

			response = Response.ok(resultList).build();

			logger.debug(SOATools.buildSOAEndLogMessage(soaContext, "getPattern", parameter.toString()));

		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		} catch (Exception bex) {
			response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@GET
	@Path("reportsAndOfferOption")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getReportAndOfferOption() {

		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.info(SOATools.buildSOALogMessage(soaContext, "ReportsAndOfferOption - GET service call"));

			GetReportsAndOfferOptionTO result = reportFacade.getReportsAndOfferOption();
			response = Response.status(Status.OK).entity(result).build();

			logger.info(SOATools.buildSOALogMessage(soaContext, "ReportsAndOfferOption - GET response built"));
		} catch (RuntimeException e) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(e, logger);
		}

		return response;
	}
}
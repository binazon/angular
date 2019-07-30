package com.orange.srs.refreport.provider.service.rest.report;

import java.io.IOException;
import java.sql.SQLException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA02ReportFacade;
import com.orange.srs.refreport.model.TO.inventory.ExportInventoryReportInputsStatusTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.TO.GetReportInputTO;
import com.orange.srs.statcommon.model.TO.report.ReportInputCassandraTOList;
import com.orange.srs.statcommon.model.TO.report.ReportInputKeyTOList;
import com.orange.srs.statcommon.model.TO.report.ReportInputLocationAndFormatTO;
import com.orange.srs.statcommon.model.parameter.GetReportInputParameterList;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.report.ReportInputKeyParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

@Stateless
@Path("reportInput")
public class ReportInputService {

	private static Logger logger = Logger.getLogger(ReportInputService.class);

	@EJB
	private SOA02ReportFacade reportFacade;

	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getAllReportInput() {

		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.info(SOATools.buildSOALogMessage(soaContext, "ReportInput - GET service call"));

			GetReportInputTO result = reportFacade.getReportInputs();
			response = Response.status(Status.OK).entity(result).build();

			logger.info(SOATools.buildSOALogMessage(soaContext, "ReportInput - GET response built"));
		} catch (RuntimeException e) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(e, logger);
		}

		return response;
	}

	@POST
	@Path("export")
	@Produces(MediaType.APPLICATION_XML)
	public Response exportAllReportInputs() {

		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.info(SOATools.buildSOALogMessage(soaContext, "ReportInput - POST service call"));

			ExportInventoryReportInputsStatusTO result = null;
			try {
				result = reportFacade.exportH2ReportInputs(soaContext);
				response = Response.status(Status.OK).entity(result).build();
			} catch (ClassNotFoundException | SQLException | BusinessException | IOException e) {
				response = RestResponseFactory.makeInternalErrorResponseFactory(e, logger);
			}

			logger.info(SOATools.buildSOALogMessage(soaContext, "ReportInput - POST response built"));
		} catch (RuntimeException e) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(e, logger);
		}

		return response;
	}

	@POST
	@Path("multipleCriteria")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getReportInput(GetReportInputParameterList parameter) {

		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(parameter);

			logger.info(SOATools.buildSOALogMessage(soaContext, "multipleCriteria - GET service call"));

			GetReportInputTO result = reportFacade.getReportInputByKeys(parameter);
			response = Response.status(Status.OK).entity(result).build();

			logger.info(SOATools.buildSOALogMessage(soaContext, "multipleCriteria - GET response built"));
		} catch (RuntimeException e) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(e, logger);
		}

		return response;
	}

	@GET
	@Path("available")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getAvailableReportInput() {

		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.info(SOATools.buildSOALogMessage(soaContext, "available - GET service call"));

			ReportInputKeyTOList list = reportFacade.getAvailableReports();
			response = Response.ok(list).build();

			logger.info(SOATools.buildSOALogMessage(soaContext, "available - GET response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	@GET
	@Path("{granularity}/{sourcetimeunit}/{reportinputref}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getReportInput(@PathParam("granularity") String granularity,
			@PathParam("sourcetimeunit") String sourceTimeUnit, @PathParam("reportinputref") String reportInputRef) {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.info(SOATools.buildSOALogMessage(soaContext, "ReportInput - GET service call"));

			ReportInputKeyParameter parameter = new ReportInputKeyParameter();
			parameter.granularity = granularity;
			parameter.reportInputRef = reportInputRef;
			parameter.sourceTimeUnit = sourceTimeUnit;
			ReportInputLocationAndFormatTO reportInputAndColumnsTO = reportFacade.getReportInput(parameter);
			response = Response.ok(reportInputAndColumnsTO).build();

			logger.info(SOATools.buildSOALogMessage(soaContext, "ReportInput - GET response built"));
		} catch (BusinessException bex) {
			response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.BAD_REQUEST);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	@GET
	@Path("Cassandra")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getCassandraReportInput() {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.info(SOATools.buildSOALogMessage(soaContext, "getCassandraReportInput - GET service call"));

			ReportInputCassandraTOList assandraReportInputs = reportFacade.getCassandraReportInput(soaContext);
			response = Response.ok(assandraReportInputs).build();

			logger.info(SOATools.buildSOALogMessage(soaContext, "getCassandraReportInput - GET response built"));
		} catch (BusinessException e) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(e, logger);
		}

		return response;
	}
}

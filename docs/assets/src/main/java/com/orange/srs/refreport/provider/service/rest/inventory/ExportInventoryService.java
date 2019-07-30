package com.orange.srs.refreport.provider.service.rest.inventory;

import java.util.Calendar;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA01InventoryFacade;
import com.orange.srs.refreport.model.TO.inventory.ExportInventoryStatusTO;
import com.orange.srs.refreport.model.exception.InventoryException;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.enums.ReportOutputTypeEnum;
import com.orange.srs.statcommon.model.parameter.ExportInventoryParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.inventory.SpecificInventoryParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

/**
 * Rest services to export inventories
 * 
 * @author A128239
 */
@Stateless
@Path("exportInventory")
public class ExportInventoryService {

	private static final Logger LOGGER = Logger.getLogger(ExportInventoryService.class);

	@EJB
	SOA01InventoryFacade inventoryFacade;

	@GET
	@Path("{origin}/{groupRefId}")
	@Produces(MediaType.APPLICATION_XML)
	public Response exportH2InventoryEntity(@PathParam("groupRefId") String groupRefId,
			@PathParam("origin") String origin, @QueryParam("date") String date) {

		ExportInventoryParameter parameter = getExportInventoryParameter(groupRefId, origin, date);

		SOAContext soaContext = SOATools.buildSOAContext(parameter);
		SOATools.buildSOAParameter(soaContext, parameter);

		Response response = null;
		try {
			LOGGER.info(SOATools.buildSOAStartLogMessage(soaContext, "exportH2InventoryEntity", parameter.toString()));

			ExportInventoryStatusTO exportInventoryStatusTO = inventoryFacade.exportH2Inventory(parameter, soaContext);
			response = Response.ok().entity(exportInventoryStatusTO).build();

			LOGGER.info(SOATools.buildSOAEndLogMessage(soaContext, "exportH2InventoryEntity", parameter.toString()));
		} catch (BusinessException bex) {
			response = RestResponseFactory.makeExceptionResponseFactory(bex.getMessage(), Status.ACCEPTED);
		} catch (Exception iex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(iex, LOGGER);
		}
		return response;
	}

	@POST
	@Path("specific")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response exportH2SpecificInventory(SpecificInventoryParameter specificInventoryParameter) {

		// ExportInventoryParameter parameter = getExportInventoryParameter(groupRefId,
		// origin, date);

		SOAContext soaContext = SOATools.buildSOAContext(specificInventoryParameter);
		SOATools.buildSOAParameter(soaContext, specificInventoryParameter);

		Response response = null;
		try {
			LOGGER.info(SOATools.buildSOAStartLogMessage(soaContext, "exportH2SpecificInventory",
					specificInventoryParameter.toString()));

			String result = inventoryFacade.exportH2SpecificInventory(specificInventoryParameter, soaContext);
			response = Response.ok().entity(result).build();

			LOGGER.info(SOATools.buildSOAEndLogMessage(soaContext, "exportH2SpecificInventory",
					specificInventoryParameter.toString()));
		} catch (Exception iex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(iex, LOGGER);
		}
		return response;
	}

	/**
	 * Export inventory report type {@link ReportOutputTypeEnum#INTERACTIVE}
	 * 
	 * @param groupRefId
	 *            Reporting group id
	 * @param origin
	 *            Origin
	 * @param date
	 *            Requested date
	 * @return {@link Status#OK}: successful case<br>
	 *         {@link Status#INTERNAL_SERVER_ERROR}: error case<br>
	 */
	@GET
	@Path("report/{origin}/{groupRefId}")
	@Produces(MediaType.APPLICATION_XML)
	public Response exportInventoryReportInteractive(@PathParam("groupRefId") String groupRefId,
			@PathParam("origin") String origin, @QueryParam("date") String date) {
		return exportInventoryReport(groupRefId, origin, date, ReportOutputTypeEnum.INTERACTIVE);
	}

	/**
	 * Export inventory report type {@link ReportOutputTypeEnum#TEMPLATE}
	 * 
	 * @param groupRefId
	 *            Reporting group id
	 * @param origin
	 *            Origin
	 * @param date
	 *            Requested date
	 * @return {@link Status#OK}: successful case<br>
	 *         {@link Status#INTERNAL_SERVER_ERROR}: error case<br>
	 */
	@GET
	@Path("report/template/{origin}/{groupRefId}")
	@Produces(MediaType.APPLICATION_XML)
	public Response exportInventoryReportTemplate(@PathParam("groupRefId") String groupRefId,
			@PathParam("origin") String origin, @QueryParam("date") String date) {
		return exportInventoryReport(groupRefId, origin, date, ReportOutputTypeEnum.TEMPLATE);
	}

	private Response exportInventoryReport(String groupRefId, String origin, String date,
			ReportOutputTypeEnum reportOutputType) {

		ExportInventoryParameter parameter = getExportInventoryParameter(groupRefId, origin, date);

		// build SOA Context for logging
		SOAContext soaContext = SOATools.buildSOAContext(parameter);
		// add new context to parameter
		SOATools.buildSOAParameter(soaContext, parameter);

		Response response = null;
		try {
			LOGGER.info(SOATools.buildSOAStartLogMessage(soaContext, "exportInventory", parameter.toString()));

			inventoryFacade.exportInventoryReport(parameter, soaContext, reportOutputType);
			response = Response.ok().build();

			LOGGER.info(SOATools.buildSOAEndLogMessage(soaContext, "exportInventory", parameter.toString()));
		} catch (InventoryException iex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(iex, LOGGER);
		} catch (RuntimeException e) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(e, LOGGER);
		}
		return response;
	}

	private static ExportInventoryParameter getExportInventoryParameter(String groupRefId, String origin, String date) {
		ExportInventoryParameter parameter = new ExportInventoryParameter();
		parameter.reportingGroupRefId = groupRefId;
		parameter.origin = origin;
		if (date != null && !date.isEmpty()) {
			String[] tmp = date.split("/");
			if (tmp.length == 3) {
				Calendar cal = Calendar.getInstance();
				cal.set(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]) - 1, Integer.parseInt(tmp[2]), 0, 0, 0);
				parameter.date = cal;
			}
		}
		return parameter;
	}
}
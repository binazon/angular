package com.orange.srs.refreport.provider.service.rest.catalog;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA03CatalogFacade;
import com.orange.srs.refreport.model.external.OfferOptionTOList;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.TO.GetBatchReportOptimizedTOList;
import com.orange.srs.statcommon.model.TO.ReportingGroupLocationTOList;
import com.orange.srs.statcommon.model.enums.OfferOptionTypeEnum;
import com.orange.srs.statcommon.model.enums.OriginEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

/**
 * Rest services to manipulate OfferOption
 * 
 * @author A159138
 */
@Stateless
@Path("offerOption")
public class OfferOptionService {

	@EJB
	private SOA03CatalogFacade catalogFacade;

	private static Logger logger = Logger.getLogger(OfferOptionService.class);

	/**
	 * GET method of all instances of OfferOption
	 * 
	 * @return 200 0K : All OfferOptions in body
	 */
	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getAllOfferOptions(@QueryParam("type") List<OfferOptionTypeEnum> types) {
		Response response = null;
		try {
			// build SOA Context whitout parameters for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.debug(SOATools.buildSOALogMessage(soaContext, "OfferOption service call"));

			OfferOptionTOList list;
			if (types.size() > 0) {
				list = catalogFacade.getOfferOptionsByTypes(types);
			} else {
				list = catalogFacade.getAllOfferOptions();
			}
			response = Response.ok(list).build();

			logger.debug(SOATools.buildSOALogMessage(soaContext, "OfferOption response built"));

		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}
		return response;
	}

	/**
	 * GET method of filtered OfferOption
	 * 
	 * @return 200 0K : filtered OfferOptions in body
	 */
	@GET
	@Path("filtered")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getAllOfferOptionsFiltered(@QueryParam("type") List<OfferOptionTypeEnum> types,
			@QueryParam("reportingGroupRef") String reportingGroupRef,
			@QueryParam("reportingGroupOrigin") OriginEnum reportingGroupOrigin) {
		Response response = null;
		try {
			// build SOA Context whitout parameters for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.debug(SOATools.buildSOALogMessage(soaContext, "OfferOption filtered service call"));

			OfferOptionTOList list = catalogFacade.getOfferOptionsFiltered(types, reportingGroupRef,
					reportingGroupOrigin);

			response = Response.ok(list).build();

			logger.debug(SOATools.buildSOALogMessage(soaContext, "OfferOption filtered response built"));

		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}
		return response;
	}

	@GET
	@Path("{optionAlias}/batchReports")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getBatchReportsForOption(@PathParam("optionAlias") String parameter,
			@QueryParam("filteredGroupReportConfig") @DefaultValue("true") boolean filteredGroupReportConfig) {
		GetBatchReportOptimizedTOList result = null;
		Response response = null;
		try {
			// build SOA Context without parameters for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.debug(SOATools.buildSOAStartLogMessage(soaContext, "getBatchReportsForOption", parameter));

			result = catalogFacade.getBatchReportsForOptionOptimized(parameter, filteredGroupReportConfig);
			response = Response.ok(result).build();

			logger.debug(SOATools.buildSOAEndLogMessage(soaContext, "getBatchReportsForOption", parameter));

		} catch (BusinessException bex) {
			logger.error("Error while fulfilling getBatchReportForOption Rest request - optionAlias=" + parameter, bex);
			response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.BAD_REQUEST);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger,
					"Error while fulfilling getBatchReportForOption Rest request - optionAlias=" + parameter);
		}
		return response;
	}

	@GET
	@Path("{optionAlias}/batchReportsAndReportingGroups")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getBatchReportsAndReportingGroupsForOption(@PathParam("optionAlias") String parameter) {
		GetBatchReportOptimizedTOList result = null;
		Response response = null;
		try {
			// build SOA Context without parameters for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.debug(SOATools.buildSOAStartLogMessage(soaContext, "getBatchReportsAndReportingGroupsForOfferOption",
					parameter));

			result = catalogFacade.getBatchReportsAndReportingGroupsForOfferOption(parameter);
			response = Response.ok(result).build();

			logger.debug(SOATools.buildSOAEndLogMessage(soaContext, "getBatchReportsAndReportingGroupsForOfferOption",
					parameter));

		} catch (BusinessException bex) {
			logger.error(
					"Error while fulfilling getBatchReportsAndReportingGroupsForOfferOption Rest request - optionAlias="
							+ parameter,
					bex);
			response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.BAD_REQUEST);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger,
					"Error while fulfilling getBatchReportsAndReportingGroupsForOfferOption Rest request - optionAlias="
							+ parameter);
		}
		return response;
	}

	@GET
	@Path("{optionAlias}/reportConfigs")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getReportConfigsForOption(@PathParam("optionAlias") String parameter) {
		GetBatchReportOptimizedTOList result = null;
		Response response = null;
		try {
			// build SOA Context whitout parameters for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.debug(SOATools.buildSOALogMessage(soaContext, "getReportConfigsForOption service call"));

			result = catalogFacade.getReportConfigsForOption(parameter);
			response = Response.ok(result).build();

			logger.debug(SOATools.buildSOALogMessage(soaContext, "getReportConfigsForOption response built"));
		} catch (RuntimeException ejbtex) {
			logger.error("Error while fulfilling getReportConfigsForOption Rest request - optionAlias=" + parameter,
					ejbtex);
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}
		return response;
	}

	@GET
	@Path("{optionAlias}/reportingGroups/all")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getAllReportingGroupsForOfferOption(@PathParam("optionAlias") String optionAlias) {
		Response response = null;
		try {
			// build SOA Context whitout parameters for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.debug(SOATools.buildSOALogMessage(soaContext, "OfferOption service call"));

			ReportingGroupLocationTOList list = catalogFacade.getAllReportingGroupsForOfferOption(optionAlias);
			response = Response.ok(list).build();

			logger.debug(SOATools.buildSOALogMessage(soaContext, "OfferOption response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}
		return response;
	}
}

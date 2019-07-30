package com.orange.srs.refreport.provider.service.rest.bookmark;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA02ReportFacade;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.TO.rest.GetBookmarkDirectReportTO;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

@Stateless
@Path("bookmarkDirectReport")
public class BookmarkDirectReportService {

	@EJB
	private SOA02ReportFacade reportFacade;

	private static Logger logger = Logger.getLogger(BookmarkDirectReportService.class);

	/**
	 * Retrieve the bookmarkDirectReport information for the label and entityType/entitySubType passed in parameter
	 * 
	 * @param report
	 * @return
	 */
	@GET
	@Path("{report}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getXml(@PathParam("report") String report, @QueryParam("entityType") String entityType,
			@QueryParam("entitySubType") String entitySubType) {
		Response response;
		SOAContext context = SOATools.buildSOAContext(null);
		GetBookmarkDirectReportTO getBookmarkDirectReportTO;
		try {
			getBookmarkDirectReportTO = reportFacade.getBookmarkDirectReport(report, entityType, entitySubType,
					context);
		} catch (NumberFormatException e) {
			logger.error(SOATools.buildSOALogMessage(context, "Error retreiving bookmarkDirectReport " + report), e);
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.BAD_REQUEST);
		} catch (BusinessException e) {
			logger.error(SOATools.buildSOALogMessage(context, "Error retreiving bookmarkDirectReport " + report), e);
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.BAD_REQUEST);
		}
		response = Response.ok(getBookmarkDirectReportTO).build();
		return response;
	}
}

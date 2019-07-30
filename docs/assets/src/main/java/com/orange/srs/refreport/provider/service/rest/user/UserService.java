package com.orange.srs.refreport.provider.service.rest.user;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA04UserFacade;
import com.orange.srs.refreport.model.external.user.CreateReportUserParameter;
import com.orange.srs.refura.model.external.UpdateUserReportingGroupParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

@Stateless
@Path("user")
public class UserService {

	@EJB
	private SOA04UserFacade userFacade;

	@Context
	private UriInfo context;

	private static Logger logger = Logger.getLogger(UserService.class);

	@POST
	@Path("reportingGroup")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response modifyLinks(UpdateUserReportingGroupParameter parameter) {
		// System.out.println("HERE "+parameter);
		SOAContext soaContext = SOATools.buildSOAContext(parameter);

		Long start = Utils.getTime();
		logger.info(SOATools.buildSOALogMessage(soaContext, "modifyLinks=> Start with parameter : " + parameter));

		Response response = Response.ok().build();
		try {
			userFacade.updateReportUserToReportingGroupLinks(parameter, soaContext);
		} catch (RuntimeException ejbtex) {
			logger.error(SOATools.buildSOALogMessage(soaContext, "Error modifyLinks on reportUser " + parameter),
					ejbtex);
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		} catch (Exception bex) {
			logger.error(SOATools.buildSOALogMessage(soaContext, "Error modifyLinks on reportUser " + parameter), bex);
			response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.INTERNAL_SERVER_ERROR);
		}

		Long end = Utils.getTime();
		logger.info(SOATools.buildSOALogMessage(soaContext, "modifyLinks=> End in : " + (end - start) + " ms"));

		return response;
	}

	/**
	 * create a reportUser in database
	 * 
	 * @param parameter
	 * @return
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response setReportUser(CreateReportUserParameter parameter) {

		SOAContext soaContext = SOATools.buildSOAContext(parameter);
		;
		Long start = Utils.getTime();
		logger.info(SOATools.buildSOALogMessage(soaContext, "setReportUser=> Start with parameter : " + parameter));

		Response response;
		try {
			userFacade.createReportUser(parameter, soaContext);
			response = Response.created(null).build();
		} catch (Exception e) {
			logger.error(SOATools.buildSOALogMessage(soaContext,
					"Error creating reportUser " + Long.toString(parameter.getReportUserId())), e);
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.BAD_REQUEST);
		}

		Long end = Utils.getTime();
		logger.info(SOATools.buildSOALogMessage(soaContext, "setReportUser=> End in : " + (end - start) + " ms"));
		return response;
	}
}

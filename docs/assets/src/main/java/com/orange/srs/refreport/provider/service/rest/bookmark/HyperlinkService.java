package com.orange.srs.refreport.provider.service.rest.bookmark;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA02ReportFacade;
import com.orange.srs.refreport.model.TO.HyperlinkTO;
import com.orange.srs.refreport.model.parameter.CreateHyperlinkParameter;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refura.model.external.HyperlinkParameter;
import com.orange.srs.statcommon.model.TO.rest.GetHyperlinkTO;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

@Stateless
@Path("hyperlink")
public class HyperlinkService {

	@EJB
	private SOA02ReportFacade reportFacade;

	private static Logger logger = Logger.getLogger(HyperlinkService.class);

	/**
	 * create a hyperlink according to the CreateHyperlinkkParameter passed in parameter
	 * 
	 * @param parameter
	 * @return
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response setHyperlink(CreateHyperlinkParameter parameter) {

		SOAContext context = SOATools.buildSOAContext(null);
		;
		Response response;
		try {
			reportFacade.createHyperlink(parameter, context);
			response = Response.created(null).build();
		} catch (BusinessException e) {
			logger.error(SOATools.buildSOALogMessage(context, "Error creating hyperlink " + parameter.getLabel()), e);
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.BAD_REQUEST);
		}
		return response;
	}

	/**
	 * This REST service allows to create or update a hyperlink
	 * 
	 * @param SUIUser
	 * @param mode
	 *            (creation or update)
	 * @param indicator
	 * @param group
	 * @param service
	 * @param favoriteId
	 * @param entityId
	 * @param period
	 * @param origin
	 * @return the id of the created bookmark in case of a creation (in JSON)
	 */
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateHyperlink(HyperlinkParameter parameter)
	// public Response createOrUpdateHyperlink(HyperlinkParameter parameter)
	{
		SOAContext context = SOATools.buildSOAContext(null);

		/*
		 * if (parameter.mode.equalsIgnoreCase("update")) {
		 */
		Response response = null;
		CreateHyperlinkParameter updateParameter = new CreateHyperlinkParameter();
		updateParameter.setLabel(parameter.label);

		try {
			HyperlinkTO hyperlinkTO = reportFacade.updateHyperlink(updateParameter, context);
			response = Response.ok(hyperlinkTO).build();
		} catch (BusinessException e) {
			logger.error(SOATools.buildSOALogMessage(context, "Error updating hyperlink " + updateParameter.getLabel()),
					e);
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.CONFLICT);
		}
		return response;
		/*
		 * }else if (parameter.mode.equalsIgnoreCase("creation")) { Response response; CreateHyperlinkParameter
		 * createParameter = new CreateHyperlinkParameter(); createParameter.setGranularity(parameter.granularity);
		 * createParameter.setReportTimeUnit(parameter.reportTimeUnit);
		 * createParameter.setIndicator(parameter.indicator); createParameter.setLabel(parameter.label); try{
		 * HyperlinkTO hyperlinkTO=reportFacade.createHyperlink(createParameter, context);
		 * response=Response.status(com.sun.jersey.api.client.ClientResponse.Status.
		 * CREATED).entity(hyperlinkTO).build(); }catch(BusinessException e) {
		 * logger.error(SOATools.buildSOALogMessage(context,"Error creating hyperlink " + createParameter.getLabel() ),
		 * e); return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.CONFLICT); } return
		 * response; } return RestResponseFactory.makeExceptionResponseFactory("mode="+ parameter.mode +
		 * " is an incorrect value" ,Status.CONFLICT);
		 */
	}

	/**
	 * Delete the hyperlink identified by the favoriteLabel passed in parameter
	 * 
	 * @param favoriteLabel
	 * @return
	 */
	@DELETE
	@Path("{favoriteLabel}")
	public Response deleteXml(@PathParam("favoriteLabel") String favoriteLabel) {
		Response response;
		SOAContext context = SOATools.buildSOAContext(null);

		try {
			reportFacade.deleteHyperlink(favoriteLabel, context);
		} catch (NumberFormatException e) {
			logger.error(SOATools.buildSOALogMessage(context, "Error deleting hyperlink " + favoriteLabel), e);
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.BAD_REQUEST);
		} catch (BusinessException e) {
			logger.error(SOATools.buildSOALogMessage(context, "Error deleting hyperlink " + favoriteLabel), e);
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.BAD_REQUEST);
		}
		response = Response.ok().build();
		return response;
	}

	/**
	 * Retrieve the hyperlink information for the favoriteLabel passed in parameter
	 * 
	 * @param favoriteLabel
	 * @return
	 */
	@GET
	@Path("{label}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getXml(@PathParam("label") String label) {
		Response response;
		SOAContext context = SOATools.buildSOAContext(null);
		GetHyperlinkTO getHyperlinkTO;
		try {
			getHyperlinkTO = reportFacade.getHyperlink(label, context);
		} catch (NumberFormatException e) {
			logger.error(SOATools.buildSOALogMessage(context, "Error retreiving hyperlink " + label), e);
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.BAD_REQUEST);
		} catch (BusinessException e) {
			logger.error(SOATools.buildSOALogMessage(context, "Error retreiving hyperlink " + label), e);
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.BAD_REQUEST);
		}
		response = Response.ok(getHyperlinkTO).build();
		return response;
	}
}

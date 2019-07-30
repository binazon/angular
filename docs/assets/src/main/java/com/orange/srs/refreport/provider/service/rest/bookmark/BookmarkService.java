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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA04UserFacade;
import com.orange.srs.refreport.model.TO.FavoriteTO;
import com.orange.srs.refreport.model.parameter.CreateBookmarkParameter;
import com.orange.srs.refreport.model.parameter.UpdateBookmarkParameter;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refura.model.external.BookmarkParameter;
import com.orange.srs.statcommon.model.TO.rest.GetBookmarkTO;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

@Stateless
@Path("bookmark")
public class BookmarkService {

	@EJB
	private SOA04UserFacade userFacade;

	@Context
	private UriInfo context;

	private static Logger logger = Logger.getLogger(BookmarkService.class);

	/**
	 * create a bookmark according to the CreateBookmarkParameter passed in parameter
	 * 
	 * @param parameter
	 * @return
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response setBookMark(CreateBookmarkParameter parameter) {

		SOAContext context = SOATools.buildSOAContext(null);
		;
		Response response;

		try {
			userFacade.createBookmark(parameter, context);
			response = Response.created(null).build();
		} catch (BusinessException e) {
			logger.error(SOATools.buildSOALogMessage(context, "Error creating bookmark " + parameter.getBookmarkId()),
					e);
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.BAD_REQUEST);
		}
		return response;
	}

	/**
	 * This REST service allows to create or update a bookmark
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
	public Response createOrUpdateBookmark(BookmarkParameter parameter) {
		SOAContext context = SOATools.buildSOAContext(null);

		if (parameter.mode.equalsIgnoreCase("update")) {
			Response response = null;
			UpdateBookmarkParameter updateParameter = new UpdateBookmarkParameter();
			updateParameter.setBookmarkId(parameter.favoriteId);
			updateParameter.setGranularity(parameter.granularity);
			updateParameter.setReportTimeUnit(parameter.reportTimeUnit);
			updateParameter.setReportingEntityId(parameter.entityId);
			updateParameter.setFilterId(parameter.filterId);
			updateParameter.setOfferOption(parameter.service);

			try {
				FavoriteTO favoriteTO = userFacade.updateBookmark(updateParameter, context);
				response = Response.ok(favoriteTO).build();
			} catch (BusinessException e) {
				logger.error(SOATools.buildSOALogMessage(context,
						"Error updating bookmark " + updateParameter.getBookmarkId()), e);
				return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.CONFLICT);
			}
			return response;
		} else if (parameter.mode.equalsIgnoreCase("creation")) {
			Response response;
			CreateBookmarkParameter createParameter = new CreateBookmarkParameter();
			createParameter.setGranularity(parameter.granularity);
			createParameter.setReportTimeUnit(parameter.reportTimeUnit);
			createParameter.setSRSId(parameter.SRSId);
			createParameter.setReportingEntityId(parameter.entityId);
			createParameter.setReportingGroupName(parameter.group);
			createParameter.setOrigin(parameter.origin);
			createParameter.setIndicator(parameter.indicator);
			createParameter.setOfferOption(parameter.service);
			createParameter.setFilterId(parameter.filterId);
			try {
				FavoriteTO favoriteTO = userFacade.createBookmark(createParameter, context);
				response = Response.status(Status.CREATED).entity(favoriteTO).build();
			} catch (BusinessException e) {
				logger.error(SOATools.buildSOALogMessage(context,
						"Error creating bookmark " + createParameter.getBookmarkId()), e);
				return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.CONFLICT);
			}
			return response;
		}
		return RestResponseFactory.makeExceptionResponseFactory("mode=" + parameter.mode + " is an incorrect value",
				Status.CONFLICT);
	}

	/**
	 * Delete the bookmark identified by the favoriteId passed in parameter
	 * 
	 * @param favoriteId
	 * @return
	 */
	@DELETE
	@Path("{favoriteId}")
	public Response deleteXml(@PathParam("favoriteId") String favoriteId) {
		Response response;
		SOAContext context = SOATools.buildSOAContext(null);

		try {
			userFacade.deleteBookmark(favoriteId, context);
		} catch (NumberFormatException e) {
			logger.error(SOATools.buildSOALogMessage(context, "Error deleting bookmark " + favoriteId), e);
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.BAD_REQUEST);
		} catch (BusinessException e) {
			logger.error(SOATools.buildSOALogMessage(context, "Error deleting bookmark " + favoriteId), e);
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.BAD_REQUEST);
		}
		response = Response.ok().build();
		return response;
	}

	/**
	 * Retrieve the bookmark information for the favoriteId passed in parameter
	 * 
	 * @param favoriteId
	 * @return
	 */
	@GET
	@Path("{favoriteId}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getXml(@PathParam("favoriteId") String favoriteId) {
		Response response;
		SOAContext context = SOATools.buildSOAContext(null);
		GetBookmarkTO getBookmarkTO;
		try {
			getBookmarkTO = userFacade.getBookmark(favoriteId, context);
		} catch (NumberFormatException e) {
			logger.error(SOATools.buildSOALogMessage(context, "Error retreiving bookmark " + favoriteId), e);
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.BAD_REQUEST);
		} catch (BusinessException e) {
			logger.error(SOATools.buildSOALogMessage(context, "Error retreiving bookmark " + favoriteId), e);
			return RestResponseFactory.makeExceptionResponseFactory(e.getMessage(), Status.BAD_REQUEST);
		}
		response = Response.ok(getBookmarkTO).build();
		return response;
	}
}

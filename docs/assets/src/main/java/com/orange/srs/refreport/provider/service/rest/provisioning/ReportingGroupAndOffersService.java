package com.orange.srs.refreport.provider.service.rest.provisioning;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA18ReportingGroupAndOfferProvisioningFacade;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.provisioning.CreateReportingGroupsAndOffersParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

/**
 * Rest services to provision the database
 * 
 * @author A159138
 */
@Stateless
@Path("reportingGroupAndOffer")
public class ReportingGroupAndOffersService {

	private static final Logger LOGGER = Logger.getLogger(ReportingGroupAndOffersService.class);

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

	@EJB
	public SOA18ReportingGroupAndOfferProvisioningFacade facade;

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getReportingGroups() {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Provisioning/reportingEntity - GET service call"));
			CreateReportingGroupsAndOffersParameter result = CreateReportingGroupsAndOffersParameter
					.makeRandomInstance();
			response = Response.ok().entity(result).build();
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Provisioning/reportingEntity - GET response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}

		return response;
	}

	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public Response putReportingGroups(@QueryParam("provisioningDate") String provisoningDate) {
		Response response = null;

		try {
			SOAContext soaContext = SOATools.buildSOAContext(null);
			Calendar provisioningCalendar = Calendar.getInstance();
			if (provisoningDate == null) {
				provisioningCalendar.setTime(new Date());
				provisioningCalendar.add(Calendar.DATE, -1);
			} else {
				provisioningCalendar.setTime(SDF.parse(provisoningDate));
			}

			facade.updateReportingGroupsAndOffers(provisioningCalendar.getTime(), soaContext);
			response = Response.ok().build();
		} catch (RuntimeException | ParseException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}

		return response;
	}

}

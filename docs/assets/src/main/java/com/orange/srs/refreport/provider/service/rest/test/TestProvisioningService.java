package com.orange.srs.refreport.provider.service.rest.test;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.applicative.helper.ProvisioningHelper;
import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.SOA18ReportingGroupAndOfferProvisioningFacade;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

@Stateless
@Path("testProvisioning")
public class TestProvisioningService {

	private static final Logger LOGGER = Logger.getLogger(TestProvisioningService.class);

	@EJB
	private ProvisioningHelper provisioningHelper;

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private SOA18ReportingGroupAndOfferProvisioningFacade reportingGroupProvisioningFacade;

	@POST
	@Path("reportingGroup/dataLocation")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response updateReportingGroupsDataLocation() {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"Update reporting groups data location - POST service call"));

			reportingGroupProvisioningFacade.updateReportingGroupsDataLocation(soaContext);
			response = Response.ok().build();

			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"Update reporting groups data location - POST response built"));
		} catch (Exception e) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(e, LOGGER);
		}
		return response;
	}

}

package com.orange.srs.refreport.provider.service.rest.catalog;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA03CatalogFacade;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

@Stateless
@Path("groupReportConfig")
public class GroupReportConfigService {

	@EJB
	SOA03CatalogFacade catalogFacade;

	private static Logger LOGGER = Logger.getLogger(GroupReportConfigService.class);

	@PUT
	@Path("update")
	@Consumes(MediaType.APPLICATION_XML)
	public Response updateGroupReportConfigs() {
		Response response = null;
		try {
			// build SOA Context whitout parameters for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "updateGroupReportConfigs service call"));

			catalogFacade.updateGroupReportConfig(soaContext);
			response = Response.ok().build();

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "updateGroupReportConfigs response built"));
		} catch (RuntimeException ejbtex) {
			LOGGER.error("Error while fulfilling updateGroupReportConfigs Rest request");
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		}
		return response;
	}
}

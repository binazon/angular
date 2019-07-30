package com.orange.srs.refreport.provider.service.rest.test;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.applicative.helper.ProvisioningHelper;
import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.SOA15PartitioningFacade;
import com.orange.srs.refreport.technical.xml.JAXBRefReportFactory;
import com.orange.srs.statcommon.model.TO.JobTO.JobSummaryTO;
import com.orange.srs.statcommon.model.enums.ProvisioningTypeEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.jobparameter.ProvisioningJobParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

/**
 * Test Class for rest unit test
 */

@Stateless
@Path("test/provisioning")
public class TestProvisioning {

	private static final Logger LOGGER = Logger.getLogger(TestProvisioning.class);

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private ProvisioningHelper provisioningHelper;

	@EJB
	private SOA15PartitioningFacade partitioningFacade;

	@GET
	@Path("/get/clientstat/{origin}")
	@Produces("application/xml")
	public String readClientStat(@PathParam("origin") String origin) {
		String res = "OK";
		SOAContext soaContext = SOATools.buildSOAContext(null);
		// try {
		// helper.readClientStat(CLIENTSTAT, origin, "REFO", soaContext);
		// } catch (BusinessException e) {
		// logger.error("CCH TEST error 1");
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		return res;
	}

	private String marshallStatusTO(Object o) {
		try {
			StringWriter resultWriter = new StringWriter();
			JAXBRefReportFactory.getMarshaller().marshal(o, resultWriter);
			return resultWriter.toString();
		} catch (JAXBException jaxbException) {
			LOGGER.error("Canno't marshall object", jaxbException);
		}
		return "";
	}

	@POST
	@Path("type")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.TEXT_PLAIN)
	public Response doProvisioning(@QueryParam("provisioningType") ProvisioningTypeEnum provisioningType,
			@QueryParam("provisioningDate") String provisoningDate) {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			LOGGER.info(SOATools.buildSOAStartLogMessage(soaContext, "Do provisioning", provisioningType.getValue()));
			Calendar provisioningCalendar = Calendar.getInstance();
			if (provisoningDate == null) {
				provisioningCalendar.setTime(new Date());
				provisioningCalendar.add(Calendar.DATE, -1);
			} else {
				provisioningCalendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(provisoningDate));
			}

			ProvisioningJobParameter parameter = new ProvisioningJobParameter();
			parameter.provisioningDate = provisioningCalendar;
			parameter.provisioningType = provisioningType;
			provisioningHelper.doProvisioningAsynchronous(soaContext, parameter, new JobSummaryTO());

			response = Response.ok("Provisioning " + provisioningType + " launched").build();

			LOGGER.info(SOATools.buildSOAEndLogMessage(soaContext, "Do provisioning", provisioningType.getValue()));
		} catch (Exception ex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ex, LOGGER);
		}
		return response;
	}
}

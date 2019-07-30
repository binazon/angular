package com.orange.srs.refreport.provider.service.rest.provisioning;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.applicative.WatcherProvisioningFiles;
import com.orange.srs.refreport.applicative.helper.ProvisioningHelper;
import com.orange.srs.refreport.business.SOA01InventoryFacade;
import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.statcommon.model.enums.ProvisioningTypeEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.jobparameter.ProvisioningJobParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

/**
 * Rest services to provision the database
 * 
 * @author A159138
 */
@Stateless
@Path("provisioning")
public class ProvisioningService {

	private static final Logger LOGGER = Logger.getLogger(ProvisioningService.class);

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

	@EJB
	private ProvisioningHelper provisioningHelper;

	@EJB
	private WatcherProvisioningFiles watcherProvisioningFiles;

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private SOA01InventoryFacade inventoryFacade;

	@GET
	@Path("yellowPart/timer/start")
	@Produces(MediaType.APPLICATION_XML)
	public Response startTimerOnYellowPartProvisioning() {
		Response response = null;
		if (watcherProvisioningFiles.startTimerIfPossible()) {
			response = Response.ok().build();
		} else {
			response = Response.ok("Timer on yellow part provsioning already started").build();
		}
		return response;
	}

	@GET
	@Path("yellowPart/timer/stop")
	@Produces(MediaType.APPLICATION_XML)
	public Response stopTimerOnYellowPartProvisioning() {
		Response response = null;
		if (watcherProvisioningFiles.stopYellowTimer()) {
			response = Response.ok().build();
		} else {
			response = RestResponseFactory.makeExceptionResponseFactory("Timer on yellow part provsioning not found",
					Status.NOT_FOUND);
		}
		return response;
	}

	@GET
	@Path("yellowPart/force")
	@Produces(MediaType.APPLICATION_XML)
	public Response stopYellowTimerAndForceUpdateFromFilesToDataBase() {
		Response response = null;
		if (watcherProvisioningFiles.forceUpdateFromFilesToDataBase()) {
			response = Response.ok().build();
		} else {
			response = RestResponseFactory.makeExceptionResponseFactory(
					"Forbidden action for the time being: there is already a check of provisioning files in progress",
					Status.FORBIDDEN);
		}
		return response;
	}

	@GET
	@Path("export")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.TEXT_PLAIN)
	public Response exportObject() {
		Response response = null;
		try {
			SOAContext soaContext = SOATools.buildSOAContext(null);
			provisioningFacade.exportObjects(soaContext);
			response = Response.ok("Export done").build();
		} catch (Exception ex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ex, LOGGER);
		}
		return response;
	}

	@Deprecated
	@POST
	@Path("reportingGroup/refobject/migrate")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.TEXT_PLAIN)
	public Response migrateRefReportReportingGroupsToRefObjectReportingGroups(
			@QueryParam("simulateMigration") @DefaultValue("true") boolean simulateMigration) {
		Response response = null;
		try {
			SOAContext soaContext = SOATools.buildSOAContext(null);
			provisioningFacade.migrateRefReportReportingGroupsToRefObjectReportingGroups(soaContext, simulateMigration);
			response = Response.ok().build();
		} catch (Exception ex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ex, LOGGER);
		}
		return response;
	}

	@POST
	@Path("refobject")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.TEXT_PLAIN)
	public Response importProvisioningFilesAndUpateData(@QueryParam("provisioningDate") String provisoningDate,
			@QueryParam("provisioningDiff") @DefaultValue("true") boolean provisioningDiff) {
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

			ProvisioningJobParameter provisioningJobParameter = new ProvisioningJobParameter();
			provisioningJobParameter.provisioningDate = provisioningCalendar;
			provisioningJobParameter.provisioningType = provisioningDiff ? ProvisioningTypeEnum.IMPORT_DIFF_AND_UPDATE
					: ProvisioningTypeEnum.IMPORT_FULL_AND_UPDATE;
			provisioningHelper.importProvisioningFilesAndUpdateDataAsynchronous(soaContext, provisioningJobParameter,
					provisioningDiff);

			response = Response.ok("Import launched").build();
		} catch (Exception ex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ex, LOGGER);
		}
		return response;
	}

}

package com.orange.srs.refreport.provider.service.rest.source;

import java.net.URI;
import java.security.KeyException;

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

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.SOA06SourceFacade;
import com.orange.srs.refreport.model.TO.provisioning.SourceClassListProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.TO.report.InputSourceConfigurationTOList;
import com.orange.srs.statcommon.model.TO.report.InputSourceKey;
import com.orange.srs.statcommon.model.TO.report.InputSourceParameterList;
import com.orange.srs.statcommon.model.TO.report.SourceClassTO;
import com.orange.srs.statcommon.model.TO.report.SourceClassTOList;
import com.orange.srs.statcommon.model.TO.report.SourceDefinition;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

/**
 * @author A129174 Rest services to manipulate inputClass
 * 
 */
@Stateless
@Path("sourceClass")
public class SourceClassService {

	@Context
	private UriInfo context;

	private static Logger logger = Logger.getLogger(SourceClassService.class);

	@EJB
	private SOA06SourceFacade sourceFacade;

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	/**
	 * 
	 * @return 200 0K : All SourceClass in body
	 */
	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getAllSourceClass() {
		Response response = null;
		try {
			SourceClassTOList list = sourceFacade.getAllSourceClass();
			response = Response.ok(list).build();
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response putReportInputClass(SourceClassTO parameter) {
		Response response = null;
		try {
			URI uri = context.getAbsolutePath();
			String key = sourceFacade.createSourceClass(parameter);
			try {
				uri = new URI(context.getAbsolutePath().toString() + "/" + key);
			} catch (Exception e) {
				uri = context.getAbsolutePath();
			}
			response = Response.created(uri).build();
		} catch (BusinessException bex) {
			response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.CONFLICT);
		} catch (KeyException kex) {
			response = RestResponseFactory.makeExceptionResponseFactory(kex, Status.CONFLICT);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	@DELETE
	@Path("/{sourceClass}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response deleteReportInputClass(@PathParam("sourceClass") String reportInputClass) {
		Response response = null;
		try {
			sourceFacade.deleteSourceClass(reportInputClass);
			response = Response.ok().build();
		} catch (BusinessException bex) {
			response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.BAD_REQUEST);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}
		return response;
	}

	@POST
	@Path("/update")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response updateSourceClassByDifferential(SourceClassListProvisioningTO sourceClassListProvisioningTO) {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.info(SOATools.buildSOALogMessage(soaContext, "SourceClass - POST update differential service call"));

			SourceClassListProvisioningTO sourceClassListProvisioningTOUpdated = provisioningFacade
					.updateSourceClassByDifferentialMarshalToFileAndRollbackIfNecessary(soaContext,
							sourceClassListProvisioningTO.sourceClassProvisioningTOs, false);
			response = Response.ok(sourceClassListProvisioningTOUpdated).build();

			logger.info(
					SOATools.buildSOALogMessage(soaContext, "SourceClass - POST update differential response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	@GET
	@Path("{sourceClass}/inputSources")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getAllInputSources(@PathParam("sourceClass") String sourceClass) {
		// build SOA Context for logging
		SOAContext soaContext = SOATools.buildSOAContext(null);

		Response response = null;
		try {
			InputSourceConfigurationTOList list = sourceFacade.getAllInputSources(sourceClass, soaContext);
			response = Response.ok(list).build();
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	@GET
	@Path("{sourceClass}/inputSourcesWithProxies")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getAllInputSourcesWithProxies(@PathParam("sourceClass") String sourceClass) {
		// build SOA Context for logging
		SOAContext soaContext = SOATools.buildSOAContext(null);

		Response response = null;
		try {
			InputSourceParameterList list = sourceFacade.getAllInputSourcesWithProxies(sourceClass, soaContext);
			response = Response.ok(list).build();
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	@GET
	@Path("{sourceClass}/{sourceName}/definition")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getInputSourcesDefinition(@PathParam("sourceClass") String sourceClass,
			@PathParam("sourceName") String sourceName) {
		// build SOA Context for logging
		SOAContext soaContext = SOATools.buildSOAContext(null);

		Response response = null;
		try {
			InputSourceKey iskey = new InputSourceKey();
			iskey.sourceClass = sourceClass;
			iskey.sourceName = sourceName;

			SourceDefinition definition = sourceFacade.getSourceDefinition(iskey, soaContext);
			response = Response.ok(definition).build();
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		} catch (BusinessException e) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(e, logger);
		}

		return response;
	}
}

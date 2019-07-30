package com.orange.srs.refreport.provider.service.rest.source;

import java.net.URI;

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
import com.orange.srs.refreport.model.TO.provisioning.ProxyListProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.TO.report.GetProxyTOList;
import com.orange.srs.statcommon.model.TO.report.ProxyTO;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

/**
 * @author A129174 Rest services to manipulate proxy
 * 
 */
@Stateless
@Path("proxy")
public class ProxyService {

	@Context
	private UriInfo context;

	private static Logger logger = Logger.getLogger(ProxyService.class);

	@EJB
	private SOA06SourceFacade sourceFacade;

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	/**
	 * 
	 * @return 200 0K : All Proxies in body
	 */
	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getProxies() {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.info(SOATools.buildSOALogMessage(soaContext, "Proxy - GET service call"));

			GetProxyTOList list = sourceFacade.getProxies(soaContext);
			response = Response.ok(list).build();

			logger.info(SOATools.buildSOALogMessage(soaContext, "Proxy - GET response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	/**
	 * 
	 * @return 201 created
	 * @return 405 Bad Request : Body and/or url is wrong
	 * @return 409 conflict : resource already exists
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response putProxy(ProxyTO parameter) {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.info(SOATools.buildSOALogMessage(soaContext, "Proxy - PUT service call"));

			URI uri = context.getAbsolutePath();
			Long key = sourceFacade.createProxy(parameter, soaContext);
			try {
				uri = new URI(context.getAbsolutePath().toString() + "/" + key);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			response = Response.created(uri).build();

			logger.info(SOATools.buildSOALogMessage(soaContext, "Proxy - PUT response built"));
		} catch (BusinessException bex) {
			response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.CONFLICT);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	/**
	 * 
	 * @param reportInputClass
	 * @return 200, object deleted correctly
	 * @return 400, unknown object
	 */
	@DELETE
	@Path("/{proxyId}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response deleteProxy(@PathParam("proxyId") Long proxyId) {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.info(SOATools.buildSOALogMessage(soaContext, "Proxy - DELETE service call"));

			sourceFacade.deleteProxy(proxyId, soaContext);
			response = Response.ok().build();

			logger.info(SOATools.buildSOALogMessage(soaContext, "Proxy - DELETE response built"));
		} catch (BusinessException bex) {
			response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.BAD_REQUEST);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}
		return response;
	}

	/**
	 * 
	 * @return 201 created
	 * @return 405 Bad Request : Body and/or url is wrong
	 * @return 409 conflict : resource already exists
	 */
	@POST
	@Path("/{proxyId}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response postProxy(@PathParam("proxyId") Long proxyId, ProxyTO parameter) {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.info(SOATools.buildSOALogMessage(soaContext, "Proxy - POST service call"));

			sourceFacade.updateProxy(proxyId, parameter, soaContext);
			response = Response.ok().build();

			logger.info(SOATools.buildSOALogMessage(soaContext, "Proxy - POST response built"));
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
	public Response updateProxyByDifferential(ProxyListProvisioningTO proxyListProvisioningTO) {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.info(SOATools.buildSOALogMessage(soaContext, "Proxy - POST update differential service call"));

			ProxyListProvisioningTO proxyListProvisioningTOUpdated = provisioningFacade
					.updateProxyByDifferentialMarshalToFileAndRollbackIfNecessary(soaContext,
							proxyListProvisioningTO.proxyProvisioningTOs, false);
			response = Response.ok(proxyListProvisioningTOUpdated).build();

			logger.info(SOATools.buildSOALogMessage(soaContext, "Proxy - POST update differential response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

}

package com.orange.srs.refreport.provider.service.rest.source;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

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
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.SOA06SourceFacade;
import com.orange.srs.refreport.model.TO.provisioning.SourceProxyListProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refreport.technical.xml.JAXBRefReportFactory;
import com.orange.srs.statcommon.model.TO.InputSourceProxyProvisioningTO;
import com.orange.srs.statcommon.model.TO.report.AbstractInputSourceListProvisioningTO;
import com.orange.srs.statcommon.model.TO.report.InputSourceKey;
import com.orange.srs.statcommon.model.TO.report.InputSourceKeyList;
import com.orange.srs.statcommon.model.TO.report.InputSourceStatusTOList;
import com.orange.srs.statcommon.model.TO.report.InputSourceTO;
import com.orange.srs.statcommon.model.TO.report.InputSourceTOList;
import com.orange.srs.statcommon.model.TO.report.PutSourceProxyParameter;
import com.orange.srs.statcommon.model.TO.report.SourceProxyTO;
import com.orange.srs.statcommon.model.TO.report.pollingConfiguration.PollingConfiguration;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.report.GetSourceStateParameter;
import com.orange.srs.statcommon.model.parameter.report.UpdateSourceStateParameter;
import com.orange.srs.statcommon.model.parameter.report.UpdateSourceStateRestParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;
import com.orange.srs.statcommon.technical.xml.JAXBFactory;

/**
 * @author A129174 Rest services to manipulate inputClass
 *
 */
@Stateless
@Path("teleconfiguration")
public class TeleconfigurationService {

	@Context
	private UriInfo context;

	private static Logger logger = Logger.getLogger(TeleconfigurationService.class);

	@EJB
	private SOA06SourceFacade sourceFacade;

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	/**
	 *
	 * @return 200 0K : All inputClass in body
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	public Response putSource(String content) {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - PUT service call"));

			StringReader reader = new StringReader(content);
			InputSourceTO to = (InputSourceTO) JAXBFactory.getUnmarshaller().unmarshal(reader);
			InputSourceKey key = sourceFacade.saveInputSource(to, soaContext);

			if (key != null) {
				URI uri = new URI(key.sourceClass + "/" + key.sourceName);
				response = Response.created(uri).build();
			} else {
				response = Response.status(Status.FOUND).entity(to).build();
			}

			logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - PUT response built"));

		} catch (URISyntaxException urise) {
			response = RestResponseFactory.makeExceptionResponseFactory(urise, Status.INTERNAL_SERVER_ERROR);
		} catch (ClassCastException cce) {
			response = RestResponseFactory.makeExceptionResponseFactory("wrong request content " + content,
					Status.BAD_REQUEST);
		} catch (RuntimeException ejbtex) {
			ejbtex.printStackTrace();
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		} catch (BusinessException bex) {
			response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.BAD_REQUEST);
		} catch (JAXBException jaxbe) {
			response = RestResponseFactory.makeExceptionResponseFactory(jaxbe, Status.BAD_REQUEST);
		}

		return response;
	}

	/**
	 *
	 * @return 200 0K : All inputClass in body
	 */
	@POST
	@Path("/{sourceClass}/{sourceName}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response modifyInputSource(@PathParam("sourceClass") String sourceClass,
			@PathParam("sourceName") String sourceName, String content) {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);
			logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - POST service call"));

			StringReader reader = new StringReader(content);
			InputSourceTO to = (InputSourceTO) JAXBFactory.getUnmarshaller().unmarshal(reader);

			InputSourceKey key = new InputSourceKey();
			key.sourceClass = sourceClass;
			key.sourceName = sourceName;

			sourceFacade.modifyInputSource(key, to, soaContext);
			response = Response.ok().build();

			logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - POST response built"));
		} catch (ClassCastException cce) {
			response = RestResponseFactory.makeExceptionResponseFactory("wrong request content " + content,
					Status.BAD_REQUEST);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		} catch (BusinessException bex) {
			response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.BAD_REQUEST);
		} catch (JAXBException jaxbe) {
			response = RestResponseFactory.makeExceptionResponseFactory(jaxbe, Status.BAD_REQUEST);
		}

		return response;
	}

	@DELETE
	@Path("/{sourceClass}/{sourceName}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response deleteInputSource(@PathParam("sourceClass") String sourceClass,
			@PathParam("sourceName") String sourceName) {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);
			logger.debug(
					SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - DELETE input source service call"));

			InputSourceKey iskey = new InputSourceKey();
			iskey.sourceClass = sourceClass;
			iskey.sourceName = sourceName;

			sourceFacade.deleteInputSource(iskey, soaContext);
			response = Response.ok(iskey).build();

			logger.debug(
					SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - DELETE input source response built"));
		} catch (BusinessException bex) {
			response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.BAD_REQUEST);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		} catch (JAXBException je) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(je, logger);
		}

		return response;
	}

	@POST
	@Path("/{sourceClass}/update")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response updateInputSourceByDifferential(@PathParam("sourceClass") String sourceClass, String content) {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.debug(
					SOATools.buildSOALogMessage(soaContext, "InputSource - POST update differential service call"));

			AbstractInputSourceListProvisioningTO inputSourceListProvisioningTO = (AbstractInputSourceListProvisioningTO) JAXBRefReportFactory
					.getUnmarshaller().unmarshal(new StringReader(content));
			AbstractInputSourceListProvisioningTO inputSourceListProvisioningTOUpdated = provisioningFacade
					.updateInputSourceByDifferentialMarshalToFileAndRollbackIfNecessary(soaContext,
							inputSourceListProvisioningTO.inputSourceProvisioningTOs, sourceClass, false);
			response = Response.ok(inputSourceListProvisioningTOUpdated).build();

			logger.debug(
					SOATools.buildSOALogMessage(soaContext, "InputSource - POST update differential response built"));
		} catch (JAXBException jaxbe) {
			response = RestResponseFactory.makeExceptionResponseFactory(jaxbe, Status.BAD_REQUEST);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	@POST
	@Path("/sourceProxy/update")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response updateSourceProxyByDifferential(SourceProxyListProvisioningTO sourceProxyListProvisioningTO) {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);

			logger.debug(
					SOATools.buildSOALogMessage(soaContext, "SourceProxy - POST update differential service call"));

			SourceProxyListProvisioningTO sourceProxyListProvisioningTOUpdated = provisioningFacade
					.updateSourceProxyByDifferentialMarshalToFileAndRollbackIfNecessary(soaContext,
							sourceProxyListProvisioningTO.inputSourceProxyProvisioningTOs);
			response = Response.ok(sourceProxyListProvisioningTOUpdated).build();

			logger.debug(
					SOATools.buildSOALogMessage(soaContext, "SourceProxy - POST update differential response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	@PUT
	@Path("/{sourceClass}/{sourceName}/proxy/{proxyId}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response putSourceProxy(@PathParam("sourceClass") String sourceClass,
			@PathParam("sourceName") String sourceName, @PathParam("proxyId") Long proxyId,
			PutSourceProxyParameter parameter) {
		Response response = null;
		try {
			InputSourceKey iskey = new InputSourceKey();
			iskey.sourceClass = sourceClass;
			iskey.sourceName = sourceName;
			try {
				// build SOA Context for logging
				SOAContext soaContext = SOATools.buildSOAContext(parameter);
				logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - PUT service call"));

				SourceProxyTO key = sourceFacade.addProxyToResource(parameter, proxyId, iskey, soaContext);
				URI uri = new URI(key.sourceKey.sourceClass + "/" + key.sourceKey.sourceName + "/proxy/" + key.proxyId);
				response = Response.created(uri).build();

				logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - PUT response built"));
			} catch (BusinessException be) {
				response = RestResponseFactory.makeExceptionResponseFactory(be, Status.CONFLICT);
			} catch (JAXBException jaxbe) {
				response = RestResponseFactory.makeExceptionResponseFactory(jaxbe, Status.BAD_REQUEST);
			}
		} catch (URISyntaxException urise) {
			response = RestResponseFactory.makeExceptionResponseFactory(urise, Status.INTERNAL_SERVER_ERROR);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	@PUT
	@Path("sourceProxy")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response putSourceProxyTeleconf(InputSourceProxyProvisioningTO inputSourceProxyProvisioningTO) {
		Response response = null;
		try {
			InputSourceKey iskey = new InputSourceKey();
			iskey.sourceClass = inputSourceProxyProvisioningTO.sourceClass;
			iskey.sourceName = inputSourceProxyProvisioningTO.sourceName;
			try {
				// build SOA Context for logging
				SOAContext soaContext = SOATools.buildSOAContext(null);
				logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - PUT service call"));

				PutSourceProxyParameter putSourceProxyParam = new PutSourceProxyParameter();
				putSourceProxyParam.index = 1;

				// only one proxy in the list, taking first element
				if (inputSourceProxyProvisioningTO.sourceProxies != null
						&& inputSourceProxyProvisioningTO.sourceProxies.size() >= 1) {
					SourceProxyTO sourceProxyCreated = sourceFacade.addProxyToResource(putSourceProxyParam,
							inputSourceProxyProvisioningTO.sourceProxies.get(0).uri, iskey, soaContext);

					// URI uri =
					// inputSourceProxyProvisioningTO.sourceProxies.get(0).uri;
					response = Response.ok(sourceProxyCreated).build();

					logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - PUT response built"));
				} else {
					response = RestResponseFactory.makeExceptionResponseFactory("Missing sourceProxies in input",
							Status.BAD_REQUEST);
				}
			} catch (BusinessException be) {
				response = RestResponseFactory.makeExceptionResponseFactory(be, Status.CONFLICT);
			} catch (JAXBException e) {
				response = RestResponseFactory.makeInternalErrorResponseFactory(e, logger);
			}
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	@POST
	@Path("/{sourceClass}/{sourceName}/proxy/{proxyId}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response modifySourceProxy(@PathParam("sourceClass") String sourceClass,
			@PathParam("sourceName") String sourceName, @PathParam("proxyId") Long proxyId,
			PutSourceProxyParameter parameter) {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(parameter);

			// add correlation id to parameter
			SOATools.buildSOAParameter(soaContext, parameter);

			logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - POST service call"));

			InputSourceKey iskey = new InputSourceKey();
			iskey.sourceClass = sourceClass;
			iskey.sourceName = sourceName;

			SourceProxyTO key = sourceFacade.modifySourceProxy(parameter, proxyId, iskey);

			URI uri = new URI(key.sourceKey.sourceClass + "/" + key.sourceKey.sourceName + "/proxy/" + key.proxyId);

			response = Response.created(uri).build();

			logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - POST response built"));
		} catch (URISyntaxException urise) {
			response = RestResponseFactory.makeExceptionResponseFactory(urise, Status.INTERNAL_SERVER_ERROR);
		} catch (BusinessException be) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(be, logger);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	@DELETE
	@Path("/{sourceClass}/{sourceName}/proxy/{proxyId}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response deleteSourceProxy(@PathParam("sourceClass") String sourceClass,
			@PathParam("sourceName") String sourceName, @PathParam("proxyId") Long proxyId) {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);
			logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - DELETE service call"));

			InputSourceKey iskey = new InputSourceKey();
			iskey.sourceClass = sourceClass;
			iskey.sourceName = sourceName;

			sourceFacade.deleteSourceProxy(proxyId, iskey, soaContext);
			response = Response.ok().build();

			logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - DELETE response built"));
		} catch (BusinessException bex) {
			response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.BAD_REQUEST);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	@POST
	@Path("/{sourceClass}/{sourceName}/state")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response modifySourceProxyState(@PathParam("sourceClass") String sourceClass,
			@PathParam("sourceName") String sourceName, UpdateSourceStateRestParameter param) {
		Response response = null;
		try {
			InputSourceKey iskey = new InputSourceKey();
			iskey.sourceClass = sourceClass;
			iskey.sourceName = sourceName;

			UpdateSourceStateParameter parameter = new UpdateSourceStateParameter();
			PollingConfiguration conf = (PollingConfiguration) JAXBRefReportFactory.getUnmarshaller()
					.unmarshal(new StringReader(param.pollingState));
			parameter.pollingState = conf;
			parameter.sourceKey = iskey;

			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(parameter);

			// add correlation id to parameter
			SOATools.buildSOAParameter(soaContext, parameter);
			logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - POST service call"));

			sourceFacade.updateSourceProxyState(parameter);

			response = Response.ok().build();

			logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - POST response built"));
		} catch (JAXBException jaxbex) {
			response = RestResponseFactory.makeExceptionResponseFactory("WRONG CONFIGURATION, unknown format : "
					+ param.pollingState + " - Exception message : " + jaxbex.getLocalizedMessage(),
					Status.BAD_REQUEST);
		} catch (BusinessException bex) {
			response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.BAD_REQUEST);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	@GET
	@Path("/{sourceClass}/{sourceName}/state")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getSourceProxyState(@PathParam("sourceClass") String sourceClass,
			@PathParam("sourceName") String sourceName) {
		Response response = null;
		try {
			InputSourceKey iskey = new InputSourceKey();
			iskey.sourceClass = sourceClass;
			iskey.sourceName = sourceName;

			GetSourceStateParameter parameter = new GetSourceStateParameter();
			parameter.sourceKey = iskey;

			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(parameter);

			// add correlation id to parameter
			SOATools.buildSOAParameter(soaContext, parameter);
			logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - POST service call"));

			String conf = sourceFacade.getSourceProxyState(parameter);

			response = Response.ok(conf).build();

			logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - POST response built"));
		} catch (BusinessException bex) {
			response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.BAD_REQUEST);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	@GET
	@Path("/{sourceClass}/{sourceName}/status")
	@Produces(MediaType.APPLICATION_XML)
	public Response getSourceStatus(@PathParam("sourceClass") String sourceClass,
			@PathParam("sourceName") String sourceName) {
		Response response = null;
		try {

			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);
			logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - GET service call"));

			InputSourceKey iskey = new InputSourceKey();
			iskey.sourceClass = sourceClass;
			iskey.sourceName = sourceName;

			InputSourceStatusTOList list = sourceFacade.getSourceStatus(iskey, soaContext);
			response = Response.ok(list).build();

			logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - GET response built"));
		} catch (BusinessException bex) {
			response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.BAD_REQUEST);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getSources() {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);
			logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - GET service call"));

			InputSourceKeyList list = sourceFacade.getSources(soaContext);
			response = Response.ok(list).build();

			logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - GET response built"));
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

	@GET
	@Path("/{sourceClass}/{sourceName}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getSource(@PathParam("sourceClass") String sourceClass,
			@PathParam("sourceName") String sourceName) {
		Response response = null;
		try {
			// build SOA Context for logging
			SOAContext soaContext = SOATools.buildSOAContext(null);
			logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - GET service call"));

			InputSourceKey iskey = new InputSourceKey();
			iskey.sourceClass = sourceClass;
			iskey.sourceName = sourceName;

			InputSourceTOList to = sourceFacade.getSource(iskey, soaContext);
			response = Response.ok(to).build();

			logger.debug(SOATools.buildSOALogMessage(soaContext, "Teleconfiguration - GET response built"));
		} catch (BusinessException bex) {
			response = RestResponseFactory.makeExceptionResponseFactory(bex, Status.BAD_REQUEST);
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, logger);
		}

		return response;
	}

}

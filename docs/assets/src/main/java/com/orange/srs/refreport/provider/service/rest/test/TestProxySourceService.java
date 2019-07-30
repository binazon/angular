package com.orange.srs.refreport.provider.service.rest.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.orange.srs.refreport.technical.xml.JAXBRefReportFactory;
import com.orange.srs.statcommon.model.TO.report.GetProxyTOList;
import com.orange.srs.statcommon.model.TO.report.PutSourceProxyParameter;
import com.orange.srs.statcommon.model.TO.report.SourceClassTOList;
import com.orange.srs.statcommon.model.TO.report.pollingConfiguration.GKSMPSourcePollingConfiguration;
import com.orange.srs.statcommon.model.TO.rest.RestResponse;
import com.orange.srs.statcommon.model.enums.ServiceMethodEnum;
import com.orange.srs.statcommon.model.parameter.SendRestParameter;
import com.orange.srs.statcommon.model.parameter.report.UpdateSourceStateRestParameter;
import com.orange.srs.statcommon.model.parameter.rest.ServiceConfigurationParameter;
import com.orange.srs.statcommon.technical.rest.RestServiceBuilder;

/**
 * Configuration contents table Customer with ClientStat.xml
 */

@Stateless
@Path("testproxysource")
public class TestProxySourceService {

	@Context
	private UriInfo context;

	@EJB
	private RestServiceBuilder restServiceBuilder;

	/**
	 * PUT method for updating or creating an instance of ConfigCustomer
	 */
	@PUT
	@Consumes("application/xml")
	@Produces("application/xml")
	public void putXml(String content) {
	}

	@GET
	@Path("/put/{sourceclass}/{inputsource}/{proxyid}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String putClass(@PathParam("sourceclass") String sourceclass, @PathParam("inputsource") String inputsource,
			@PathParam("proxyid") Long proxyid) {
		try {
			PutSourceProxyParameter params = new PutSourceProxyParameter();
			params.index = 1;

			URI consumerUri = new URI(context.getBaseUri().toString());
			// consumer.initConsumer(consumerUri, false);
			// SendRestRequestParameter<PutSourceProxyParameter> parameter=new
			// SendRestRequestParameter<PutSourceProxyParameter>();
			// parameter.httpMethod=ServiceMethodEnum.PUT;
			// parameter.parameter=params;
			// parameter.requestMedia=MediaType.APPLICATION_XML_TYPE;
			// parameter.servicePath="teleconfiguration/"+sourceclass+"/"+inputsource+"/proxy/"+proxyid;
			//
			// RestResponse response=consumer.sendRequest(parameter);

			ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
			sparameter.uri = consumerUri;
			sparameter.httpMethod = ServiceMethodEnum.PUT;
			sparameter.requestMediaType = MediaType.APPLICATION_XML;

			SendRestParameter<PutSourceProxyParameter> sendRestParameter = new SendRestParameter<>();
			sendRestParameter.parameter = params;
			RestResponse response = restServiceBuilder.sendRequest(sendRestParameter, sparameter,
					"teleconfiguration/" + sourceclass + "/" + inputsource + "/proxy/" + proxyid);

			String retour = "";
			if (response.getResult() instanceof InputStream) {
				InputStream stream = (InputStream) response.getResult();
				InputStreamReader ireader = new InputStreamReader(stream);
				BufferedReader breader = new BufferedReader(ireader);
				String txt;
				while ((txt = breader.readLine()) != null) {
					System.out.println(txt);
				}
			}

			return response.getResponse().getLocation() + " " + response.getResponse().getStatus();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "error";
	}

	@GET
	@Path("/post/{sourceclass}/{inputsource}/{proxyid}/{index}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String postClass(@PathParam("sourceclass") String sourceclass, @PathParam("inputsource") String inputsource,
			@PathParam("proxyid") Long proxyid, @PathParam("index") int index) {

		try {
			PutSourceProxyParameter params = new PutSourceProxyParameter();
			params.index = index;
			URI consumerUri = new URI(context.getBaseUri().toString());
			System.out.println(consumerUri.toString());
			// consumer.initConsumer(consumerUri, false);
			// SendRestRequestParameter<PutSourceProxyParameter> parameter=new
			// SendRestRequestParameter<PutSourceProxyParameter>();
			// parameter.httpMethod=ServiceMethodEnum.POST;
			// parameter.parameter=params;
			// parameter.requestMedia=MediaType.APPLICATION_XML_TYPE;
			// parameter.servicePath="teleconfiguration/"+sourceclass+"/"+inputsource+"/proxy/"+proxyid;
			//
			// RestResponse response=consumer.sendRequest(parameter);

			ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
			sparameter.uri = consumerUri;
			sparameter.httpMethod = ServiceMethodEnum.POST;
			sparameter.requestMediaType = MediaType.APPLICATION_XML;

			SendRestParameter<PutSourceProxyParameter> sendRestParameter = new SendRestParameter<>();
			sendRestParameter.parameter = params;
			RestResponse response = restServiceBuilder.sendRequest(sendRestParameter, sparameter,
					"teleconfiguration/" + sourceclass + "/" + inputsource + "/proxy/" + proxyid);

			String retour = "";
			if (response.getResult() instanceof InputStream) {
				InputStream stream = (InputStream) response.getResult();
				InputStreamReader ireader = new InputStreamReader(stream);
				BufferedReader breader = new BufferedReader(ireader);
				String txt;
				while ((txt = breader.readLine()) != null) {
					System.out.println(txt);
				}
			}

			return response.getResponse().getLocation() + " " + response.getResponse().getStatus();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "error";
	}

	@GET
	@Path("/delete/{sourceclass}/{inputsource}/{proxyid}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String postClass(@PathParam("sourceclass") String sourceclass, @PathParam("inputsource") String inputsource,
			@PathParam("proxyid") Long proxyid) {

		try {
			URI consumerUri = new URI(context.getBaseUri().toString());
			System.out.println(consumerUri.toString());
			// consumer.initConsumer(consumerUri, false);
			// SendRestRequestParameter<Void> parameter=new
			// SendRestRequestParameter<Void>();
			// parameter.httpMethod=ServiceMethodEnum.DELETE;
			// parameter.requestMedia=MediaType.APPLICATION_XML_TYPE;
			// parameter.servicePath="teleconfiguration/"+sourceclass+"/"+inputsource+"/proxy/"+proxyid;
			//
			// RestResponse response=consumer.sendRequest(parameter);

			ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
			sparameter.uri = consumerUri;
			sparameter.httpMethod = ServiceMethodEnum.DELETE;
			sparameter.requestMediaType = MediaType.APPLICATION_XML;

			SendRestParameter<Void> sendRestParameter = new SendRestParameter<>();
			RestResponse response = restServiceBuilder.sendRequest(sendRestParameter, sparameter,
					"teleconfiguration/" + sourceclass + "/" + inputsource + "/proxy/" + proxyid);

			String retour = "";
			if (response.getResult() instanceof InputStream) {
				InputStream stream = (InputStream) response.getResult();
				InputStreamReader ireader = new InputStreamReader(stream);
				BufferedReader breader = new BufferedReader(ireader);
				String txt;
				while ((txt = breader.readLine()) != null) {
					System.out.println(txt);
				}
			}

			return response.getResponse().getLocation() + " " + response.getResponse().getStatus();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "error";
	}

	@GET
	@Path("get")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String getInputClasses() {
		try {
			URI consumerUri = new URI(context.getBaseUri().toString());
			System.out.println(consumerUri.toString());

			// consumer.initConsumer(consumerUri, false);
			// SendRestRequestParameter<Void> parameter=new
			// SendRestRequestParameter<Void>();
			// parameter.httpMethod=ServiceMethodEnum.GET;
			// parameter.requestMedia=MediaType.APPLICATION_XML_TYPE;
			// parameter.servicePath="proxy";
			// consumer.addRestResponseType(ServiceMethodEnum.GET, 200,
			// GetProxyTOList.class);
			// RestResponse response=consumer.sendRequest(parameter);

			restServiceBuilder.addRestResponseType(ServiceMethodEnum.GET, Status.OK.getStatusCode(),
					GetProxyTOList.class);

			ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
			sparameter.uri = consumerUri;
			sparameter.httpMethod = ServiceMethodEnum.GET;
			sparameter.requestMediaType = MediaType.APPLICATION_XML;

			SendRestParameter<Void> sendRestParameter = new SendRestParameter<>();
			RestResponse response = restServiceBuilder.sendRequest(sendRestParameter, sparameter, "proxy");

			String retour = "";
			if (response.getResult() instanceof InputStream) {
				InputStream stream = (InputStream) response.getResult();
				InputStreamReader ireader = new InputStreamReader(stream);
				BufferedReader breader = new BufferedReader(ireader);
				String txt;
				while ((txt = breader.readLine()) != null) {
					System.out.println(txt);
				}
			} else if (response.getResult() instanceof SourceClassTOList) {
				System.out.println(response.getResult().toString());
			}

			return response.getResponse().getLocation() + " " + response.getResponse().getStatus();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "error";
	}

	@GET
	@Path("update/{sourceclass}/{inputsource}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response updateProxySourceState(@PathParam("sourceclass") String sourceclass,
			@PathParam("inputsource") String inputsource) {

		try {
			UpdateSourceStateRestParameter param = new UpdateSourceStateRestParameter();
			GKSMPSourcePollingConfiguration conf = new GKSMPSourcePollingConfiguration();
			conf.lastFileDate = new Date();
			conf.pollingIndice = 10;
			StringWriter confWriter = new StringWriter();
			JAXBRefReportFactory.getMarshaller().marshal(conf, confWriter);
			param.pollingState = confWriter.toString();
			URI consumerUri = new URI(context.getBaseUri().toString());
			System.out.println(consumerUri.toString());

			// consumer.initConsumer(consumerUri, false);
			// SendRestRequestParameter<UpdateSourceStateRestParameter> parameter=new
			// SendRestRequestParameter<UpdateSourceStateRestParameter>();
			// parameter.httpMethod=ServiceMethodEnum.POST;
			// parameter.parameter=param;
			// parameter.requestMedia=MediaType.APPLICATION_XML_TYPE;
			// parameter.servicePath="teleconfiguration/"+sourceclass+"/"+inputsource+"/state";
			// //consumer.addRestResponseType(ServiceMethodEnum.POST, 200,
			// UpdateSourceProxyStateTOList.class);
			// RestResponse response=consumer.sendRequest(parameter);

			ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
			sparameter.uri = consumerUri;
			sparameter.httpMethod = ServiceMethodEnum.POST;
			sparameter.requestMediaType = MediaType.APPLICATION_XML;

			SendRestParameter<UpdateSourceStateRestParameter> sendRestParameter = new SendRestParameter<>();
			sendRestParameter.parameter = param;
			RestResponse response = restServiceBuilder.sendRequest(sendRestParameter, sparameter,
					"teleconfiguration/" + sourceclass + "/" + inputsource + "/state");

			return Response.ok(response.getResult()).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.serverError().build();

	}
}

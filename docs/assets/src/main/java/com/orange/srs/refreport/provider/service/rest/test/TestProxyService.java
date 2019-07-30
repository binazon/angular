package com.orange.srs.refreport.provider.service.rest.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.orange.srs.statcommon.model.TO.report.GetProxyTOList;
import com.orange.srs.statcommon.model.TO.report.ProxyTO;
import com.orange.srs.statcommon.model.TO.report.SourceClassTOList;
import com.orange.srs.statcommon.model.TO.rest.RestResponse;
import com.orange.srs.statcommon.model.enums.ServiceMethodEnum;
import com.orange.srs.statcommon.model.parameter.SendRestParameter;
import com.orange.srs.statcommon.model.parameter.rest.ServiceConfigurationParameter;
import com.orange.srs.statcommon.technical.rest.RestServiceBuilder;

/**
 * Configuration contents table Customer with ClientStat.xml
 */

@Stateless
@Path("testproxy")
public class TestProxyService {

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
	@Path("/put/{name}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String putClass(@PathParam("name") String name, @QueryParam("urinull") boolean urinull) {
		ProxyTO proxy = new ProxyTO();
		proxy.name = name;
		try {
			if (urinull) {
				proxy.uri = null;
			} else {

				// proxy.uri=new URI("http://10.238.35.83:8080/ProxyColl/rs/");
				proxy.uri = new URI("http://localhost:8080/RefReport/rs/testxml");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		proxy.version = "V0";

		try {
			URI consumerUri = new URI(context.getBaseUri().toString());

			// consumer.initConsumer(consumerUri, false);
			// SendRestRequestParameter<ProxyTO> parameter=new
			// SendRestRequestParameter<ProxyTO>();
			// parameter.httpMethod=ServiceMethodEnum.PUT;
			// parameter.parameter=proxy;
			// parameter.requestMedia=MediaType.APPLICATION_XML_TYPE;
			// parameter.servicePath="proxy";
			//
			// RestResponse response=consumer.sendRequest(parameter);
			//
			ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
			sparameter.uri = consumerUri;
			sparameter.httpMethod = ServiceMethodEnum.PUT;
			sparameter.requestMediaType = MediaType.APPLICATION_XML;

			SendRestParameter<ProxyTO> sendRestParameter = new SendRestParameter<>();
			sendRestParameter.parameter = proxy;
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
			}

			return response.getResponse().getLocation() + " " + response.getResponse().getStatus();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "error";
	}

	@GET
	@Path("/post/{name}/{proxyId}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String postClass(@PathParam("name") String name, @PathParam("proxyId") Long proxyId,
			@QueryParam("urinull") boolean urinull) {
		ProxyTO proxy = new ProxyTO();
		proxy.name = name;

		try {
			if (urinull) {
				proxy.uri = null;
			} else {
				proxy.uri = new URI("http://10.238.35.83:8080/ProxyColl/rs/");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		proxy.version = "V0";

		try {
			System.out.println("ID " + proxyId);
			URI consumerUri = new URI(context.getBaseUri().toString());
			// consumer.initConsumer(consumerUri, false);
			// SendRestRequestParameter<ProxyTO> parameter=new
			// SendRestRequestParameter<ProxyTO>();
			// parameter.httpMethod=ServiceMethodEnum.POST;
			// parameter.parameter=proxy;
			// parameter.requestMedia=MediaType.APPLICATION_XML_TYPE;
			// parameter.servicePath="proxy/"+proxyId;
			//
			// RestResponse response=consumer.sendRequest(parameter);

			ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
			sparameter.uri = consumerUri;
			sparameter.httpMethod = ServiceMethodEnum.POST;
			sparameter.requestMediaType = MediaType.APPLICATION_XML;

			SendRestParameter<ProxyTO> sendRestParameter = new SendRestParameter<>();
			sendRestParameter.parameter = proxy;
			RestResponse response = restServiceBuilder.sendRequest(sendRestParameter, sparameter, "proxy/" + proxyId);
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

			SendRestParameter<ProxyTO> sendRestParameter = new SendRestParameter<>();
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

}

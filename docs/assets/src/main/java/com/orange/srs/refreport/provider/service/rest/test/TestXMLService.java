package com.orange.srs.refreport.provider.service.rest.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import com.orange.srs.statcommon.model.TO.ErrorTO;
import com.orange.srs.statcommon.model.TO.report.GKSMPInputSourceTO;
import com.orange.srs.statcommon.model.TO.report.InputSourceKey;
import com.orange.srs.statcommon.model.TO.report.InputSourceStatusTO;
import com.orange.srs.statcommon.model.TO.report.InputSourceTO;
import com.orange.srs.statcommon.model.TO.report.ReportInputKeyTO;
import com.orange.srs.statcommon.model.TO.report.ReportInputKeyTOList;
import com.orange.srs.statcommon.model.TO.report.SourceClassTO;
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
@Path("testxml")
public class TestXMLService {

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
	@Path("/sourceClasses")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String getInputClasses() {
		try {
			URI consumerUri = new URI(context.getBaseUri().toString());
			System.out.println(consumerUri.toString());
			// consumer.initConsumer(consumerUri, false);
			// SendRestRequestParameter<SourceClassTO> parameter=new
			// SendRestRequestParameter<SourceClassTO>();
			// parameter.httpMethod=ServiceMethodEnum.GET;
			// parameter.requestMedia=MediaType.APPLICATION_XML_TYPE;
			// parameter.servicePath="sourceClass";
			// consumer.addRestResponseType(ServiceMethodEnum.GET, 200,
			// SourceClassTOList.class);
			// RestResponse response=consumer.sendRequest(parameter);

			restServiceBuilder.addRestResponseType(ServiceMethodEnum.GET, Status.OK.getStatusCode(),
					SourceClassTOList.class);

			ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
			sparameter.uri = consumerUri;
			sparameter.httpMethod = ServiceMethodEnum.GET;
			sparameter.requestMediaType = MediaType.APPLICATION_XML;

			SendRestParameter<SourceClassTO> sendRestParameter = new SendRestParameter<>();

			RestResponse response = restServiceBuilder.sendRequest(sendRestParameter, sparameter, "sourceClass");

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
	@Path("/sourceClass/put")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String putClass() {
		SourceClassTO list = new SourceClassTO();
		list.sourceClass = "GKSMPSource";

		ReportInputKeyTO key = new ReportInputKeyTO();
		key.reportInputId = 5L;
		key.granularity = "PERIOD";
		key.sourceTimeUnit = "DAY";
		key.reportInputRef = "AFDB_T-IPBX-traffic";
		list.getReportInputKeyTO().add(key);

		ReportInputKeyTO key2 = new ReportInputKeyTO();
		key2.reportInputId = 4L;
		key2.granularity = "MONTHLY";
		key2.sourceTimeUnit = "YEAR";
		key2.reportInputRef = "AFDB_T-CLUSTER-traffic";
		list.getReportInputKeyTO().add(key2);

		try {
			URI consumerUri = new URI(context.getBaseUri().toString());
			System.out.println(consumerUri.toString());
			// consumer.initConsumer(consumerUri, false);
			// SendRestRequestParameter<SourceClassTO> parameter=new
			// SendRestRequestParameter<SourceClassTO>();
			// parameter.httpMethod=ServiceMethodEnum.PUT;
			// parameter.parameter=list;
			// parameter.requestMedia=MediaType.APPLICATION_XML_TYPE;
			// parameter.servicePath="sourceClass";

			restServiceBuilder.addRestResponseType(ServiceMethodEnum.GET, Status.OK.getStatusCode(),
					SourceClassTOList.class);

			ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
			sparameter.uri = consumerUri;
			sparameter.httpMethod = ServiceMethodEnum.PUT;
			sparameter.requestMediaType = MediaType.APPLICATION_XML;

			SendRestParameter<SourceClassTO> sendRestParameter = new SendRestParameter<>();
			sendRestParameter.parameter = list;
			RestResponse response = restServiceBuilder.sendRequest(sendRestParameter, sparameter, "sourceClass");

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
	@Path("/sourceClass/delete")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String deleteClass() {

		try {
			URI consumerUri = new URI(context.getBaseUri().toString());
			System.out.println(consumerUri.toString());
			// consumer.initConsumer(consumerUri, false);
			// SendRestRequestParameter<SourceClassTO> parameter=new
			// SendRestRequestParameter<SourceClassTO>();
			// parameter.httpMethod=ServiceMethodEnum.DELETE;
			// parameter.requestMedia=MediaType.APPLICATION_XML_TYPE;
			// parameter.servicePath="sourceClass/GKSMPSource";

			// RestResponse response=consumer.sendRequest(parameter);

			ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
			sparameter.uri = consumerUri;
			sparameter.httpMethod = ServiceMethodEnum.DELETE;
			sparameter.requestMediaType = MediaType.APPLICATION_XML;

			SendRestParameter<SourceClassTO> sendRestParameter = new SendRestParameter<>();
			RestResponse response = restServiceBuilder.sendRequest(sendRestParameter, sparameter,
					"sourceClass/GKSMPSource");

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
	@Path("/error")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public ErrorTO getError() {
		ErrorTO error = new ErrorTO();
		error.message = "error message";
		error.comment = "error comment if necessary";
		return error;
	}

	@GET
	@Path("/input")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getInput() {
		Response rresponse = null;

		try {
			URI consumerUri = new URI(context.getBaseUri().toString());
			System.out.println(consumerUri.toString());
			// consumer.initConsumer(consumerUri, false);
			// SendRestRequestParameter<SourceClassTO> parameter=new
			// SendRestRequestParameter<SourceClassTO>();
			// parameter.httpMethod=ServiceMethodEnum.GET;
			// parameter.requestMedia=MediaType.APPLICATION_XML_TYPE;
			// parameter.servicePath="reportInput/HOURLY/DAY/AFDB_T-CLUSTER-traffic";
			// //rs/reportInput/HOURLY/DAY/AFDB_T-CLUSTER-traffic
			// RestResponse response=consumer.sendRequest(parameter);

			ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
			sparameter.uri = consumerUri;
			sparameter.httpMethod = ServiceMethodEnum.DELETE;
			sparameter.requestMediaType = MediaType.APPLICATION_XML;

			SendRestParameter<SourceClassTO> sendRestParameter = new SendRestParameter<>();
			RestResponse response = restServiceBuilder.sendRequest(sendRestParameter, sparameter,
					"reportInput/HOURLY/DAY/AFDB_T-CLUSTER-traffic");

			rresponse = Response.ok(response.getResult()).build();

			System.out.println("Code :" + response.getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rresponse;
	}

	@GET
	@Path("/inputs")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public ReportInputKeyTOList getInputs() {
		ReportInputKeyTOList list = new ReportInputKeyTOList();

		for (int i = 0; i < 2; i++) {
			ReportInputKeyTO keyto = new ReportInputKeyTO();
			keyto.granularity = "MONTHLY";
			keyto.sourceTimeUnit = "DAY";
			keyto.reportInputRef = "DATA" + i;
			list.getInputKeys().add(keyto);
		}
		return list;
	}

	@GET
	@Path("/inputSources/put")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public GKSMPInputSourceTO putSource() {

		GKSMPInputSourceTO to = new GKSMPInputSourceTO();
		to.getAuthority().host = "spaceball";
		to.getAuthority().port = "5454";
		to.getAuthority().userName = "President Esbrouffe";
		to.getAuthority().userPassword = "12345";
		to.sourceDirectory = "directory";
		to.setSourceName("sourceName");
		// to.sourceClass="SourceClass";
		return to;

	}

	@PUT
	@Path("/testsource")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String testClient(String content) {
		System.out.println(content);
		return content;
	}

	@PUT
	@Path("/teleconfiguration")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response testTeleconf(String content) {
		Response response = null;

		if (context.getBaseUri().getHost().equals("127.0.0.2")) {
			response = Response.serverError().build();

		} else {
			response = Response.ok().build();
		}
		return response;
	}

	@GET
	@Path("/teleconfiguration/{source}/{classe}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response testTeleconf(@PathParam("source") String source, @PathParam("classe") String classe) {
		Response response = null;
		GKSMPInputSourceTO to = new GKSMPInputSourceTO();
		to.getAuthority().host = "10.238.60.87";
		to.getAuthority().port = "22";
		to.getAuthority().userName = "root";
		to.getAuthority().userPassword = "_merlin";
		to.sourceDirectory = "/data/flf/01zfdb/files/rawdata/cdr/gk/equipment/GK.RECETTE_1_4";
		to.setSourceName("GK2");
		to.setPollingPeriod(3600L);

		response = Response.ok(to).build();
		return response;
	}

	@POST
	@Path("/teleconfiguration")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response testTeleconfPost(String content) {
		Response response = null;
		if (context.getBaseUri().getHost().equals("127.0.0.1")) {
			response = Response.serverError().build();

		} else {
			response = Response.ok().build();
		}
		return response;
	}

	@GET
	@Path("/teleconfiguration")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response testTeleconfGet() {
		Response response = null;
		if (context.getBaseUri().getHost().equals("127.0.0.1")) {
			response = Response.serverError().build();

		} else {
			response = Response.ok().build();
		}
		return response;
	}

	@DELETE
	@Path("/teleconfiguration/{source}/{classe}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response testTeleconfDELETE(@PathParam("source") String source, @PathParam("classe") String classe) {
		Response response = null;

		/*
		 * if(context.getBaseUri().getHost().equals("127.0.0.1")) { response=Response.serverError().build();
		 * 
		 * }else
		 */
		{
			response = Response.ok().build();
		}
		return response;
	}

	@GET
	@Path("/inputSource/put")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String putClassSource() {

		GKSMPInputSourceTO to = new GKSMPInputSourceTO();
		to.getAuthority().host = "10.238.60.87";
		to.getAuthority().port = "22";
		to.getAuthority().userName = "root";
		to.getAuthority().userPassword = "_merlin";
		to.sourceDirectory = "/data/flf/01zfdb/files/rawdata/cdr/gk/equipment/GK.RECETTE_1_4";
		to.setSourceName("GK2");
		to.setPollingPeriod(3600L);
		try {
			URI consumerUri = new URI(context.getBaseUri().toString());
			// consumer.initConsumer(consumerUri, false);
			// SendRestRequestParameter<InputSourceTO> parameter=new
			// SendRestRequestParameter<InputSourceTO>();
			// parameter.httpMethod=ServiceMethodEnum.PUT;
			// parameter.parameter=to;
			// parameter.requestMedia=MediaType.APPLICATION_XML_TYPE;
			// parameter.servicePath="teleconfiguration";

			// RestResponse response=consumer.sendRequest(parameter);

			ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
			sparameter.uri = consumerUri;
			sparameter.httpMethod = ServiceMethodEnum.PUT;
			sparameter.requestMediaType = MediaType.APPLICATION_XML;

			SendRestParameter<InputSourceTO> sendRestParameter = new SendRestParameter<>();
			sendRestParameter.parameter = to;
			RestResponse response = restServiceBuilder.sendRequest(sendRestParameter, sparameter, "teleconfiguration");

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
	@Path("/inputSource/post")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String postClassSource() {

		GKSMPInputSourceTO to = new GKSMPInputSourceTO();
		to.getAuthority().host = "spaceball";
		to.getAuthority().port = "5454";
		to.getAuthority().userName = "President Esbrouffe 2";
		to.getAuthority().userPassword = "12345";
		to.sourceDirectory = "directorynew";
		to.setSourceName("SourceName");

		try {
			URI consumerUri = new URI(context.getBaseUri().toString());
			// consumer.initConsumer(consumerUri, false);
			// SendRestRequestParameter<InputSourceTO> parameter=new
			// SendRestRequestParameter<InputSourceTO>();
			// parameter.httpMethod=ServiceMethodEnum.POST;
			// parameter.parameter=to;
			// parameter.requestMedia=MediaType.APPLICATION_XML_TYPE;
			// parameter.servicePath="teleconfiguration/"+to.getSourceClass()+"/"+to.getSourceName();
			//
			// RestResponse response=consumer.sendRequest(parameter);

			ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
			sparameter.uri = consumerUri;
			sparameter.httpMethod = ServiceMethodEnum.POST;
			sparameter.requestMediaType = MediaType.APPLICATION_XML;

			SendRestParameter<InputSourceTO> sendRestParameter = new SendRestParameter<>();
			sendRestParameter.parameter = to;
			RestResponse response = restServiceBuilder.sendRequest(sendRestParameter, sparameter,
					"teleconfiguration/" + to.getSourceClass() + "/" + to.getSourceName());
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
	@Path("/inputSource/delete")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String deleteClassSource() {

		GKSMPInputSourceTO to = new GKSMPInputSourceTO();
		to.getAuthority().host = "spaceball";
		to.getAuthority().port = "5454";
		to.getAuthority().userName = "President Esbrouffe";
		to.getAuthority().userPassword = "12345";
		to.sourceDirectory = "directorynew";
		to.setSourceName("SourceName");

		try {
			URI consumerUri = new URI(context.getBaseUri().toString());
			// consumer.initConsumer(consumerUri, false);
			// SendRestRequestParameter<Void> parameter=new
			// SendRestRequestParameter<Void>();
			// parameter.httpMethod=ServiceMethodEnum.DELETE;
			// parameter.requestMedia=MediaType.APPLICATION_XML_TYPE;
			// parameter.servicePath="teleconfiguration/"+to.getSourceClass()+"/"+to.getSourceName();
			//
			// RestResponse response=consumer.sendRequest(parameter);
			//
			ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
			sparameter.uri = consumerUri;
			sparameter.httpMethod = ServiceMethodEnum.DELETE;
			sparameter.requestMediaType = MediaType.APPLICATION_XML;

			SendRestParameter<InputSourceTO> sendRestParameter = new SendRestParameter<>();
			RestResponse response = restServiceBuilder.sendRequest(sendRestParameter, sparameter,
					"teleconfiguration/" + to.getSourceClass() + "/" + to.getSourceName());
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
	@Path("/status/{sourceClass}/{sourceName}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public InputSourceStatusTO status(@PathParam("sourceClass") String sourceClass,
			@PathParam("sourceName") String sourceName) {
		InputSourceStatusTO to = new InputSourceStatusTO();
		to.date = "20111130T124011Z";
		to.description = "ma belle description que j'aime";
		to.message = "ohhhhh le beau message";
		to.status = "OK";
		to.source = new InputSourceKey();
		to.source.sourceClass = sourceClass;
		to.source.sourceName = sourceName;

		return to;
	}
}

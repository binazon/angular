package com.orange.srs.refreport.provider.service.rest.test;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.orange.srs.refreport.technical.JMSConnectionHandler;
import com.orange.srs.statcommon.model.enums.TaskTypeEnum;
import com.orange.srs.statcommon.model.parameter.jobparameter.JobParameter;

/**
 * Test Class for JMS unit test
 */

@Stateless
@Path("testjms")
public class TestJMSSender {

	@EJB
	private JMSConnectionHandler jmsConnectionHandler;

	@GET
	@Path("/get/{taskType}/{jobId}/{queue}")
	@Consumes("application/xml")
	@Produces("application/xml")
	public Response getXml(@PathParam("taskType") String taskType, @PathParam("jobId") Long jobId,
			@PathParam("queue") String queueAlias) {
		Response response = null;
		JobParameter parameter = new JobParameter();
		parameter.setJobId(jobId);
		parameter.taskType = TaskTypeEnum.valueOf(taskType.toUpperCase());
		parameter.responseCanal = "canal3";
		jmsConnectionHandler.sendJMSMessage(parameter, queueAlias);
		response = Response.ok().build();
		return response;

	}
}

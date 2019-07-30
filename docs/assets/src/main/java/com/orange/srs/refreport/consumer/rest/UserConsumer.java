package com.orange.srs.refreport.consumer.rest;

import java.util.HashMap;

import javax.ejb.EJB;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.TO.rest.GetUserIdBySUITO;
import com.orange.srs.statcommon.model.TO.rest.RestResponse;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.SendRestParameter;
import com.orange.srs.statcommon.technical.exception.RestConsumerException;
import com.orange.srs.statcommon.technical.exception.RestServiceConsumerException;
import com.orange.srs.statcommon.technical.rest.RestServiceBuilder;

public class UserConsumer {
	private static Logger logger = Logger.getLogger(UserConsumer.class);

	@EJB
	private RestServiceBuilder restServiceBuilder;

	/**
	 * @param parameter
	 * @return
	 * @throws BusinessException
	 * @throws JAXBException
	 * @throws RestServiceConsumerException
	 * @throws RestConsumerException
	 */
	public GetUserIdBySUITO getUserIdBySUI(SOAContext soaContext, String SUIId)
			throws BusinessException, RestServiceConsumerException, JAXBException {
		// build SendRestRequestParameter object required by restServiceConsumer
		SendRestParameter<Void> parameter = new SendRestParameter<Void>();
		HashMap<String, String> pathParam = new HashMap<String, String>();
		pathParam.put("SUID", SUIId);
		parameter.pathParameters = pathParam;

		// send REST request
		RestResponse response = restServiceBuilder.sendRequestToService(RestServiceBuilder.USER_ID_BY_SUI_SERVICE,
				parameter, soaContext);
		if (response.getHttpCode() != Status.OK.getStatusCode()) {
			throw new BusinessException("SUIUserId " + SUIId + " does not have any matching SRSId in RefURA");
		}
		GetUserIdBySUITO result = (GetUserIdBySUITO) response.getResult();
		return result;
	}
}

package com.orange.srs.refreport.provider.messaging;

import javax.ejb.EJB;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.delegate.ProvisioningConsumerDelegate;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.jobparameter.JobParameter;
import com.orange.srs.statcommon.technical.SOATools;

public class ProvisioningConsumer implements MessageListener {

	private static final Logger LOGGER = Logger.getLogger(ProvisioningConsumer.class);

	@EJB
	private ProvisioningConsumerDelegate provisioningConsumerDelegate;



	public ProvisioningConsumer() {
	}

	/**
	 * @see MessageListener#onMessage(Message)
	 */
	@Override
	public void onMessage(Message message) {
		ObjectMessage omessage = (ObjectMessage) message;
		LOGGER.info("Message recu " + omessage.toString());

		try {
			JobParameter parameter = (JobParameter) omessage.getObject();

			// build SOA Context for parameter
			SOAContext soaContext = SOATools.buildSOAContext(parameter);

			provisioningConsumerDelegate.handleMessageReception(parameter, soaContext);

		} catch (Exception e) {
			LOGGER.error("Error occurs when receiving Task " + e.getMessage(), e);
		}
	}

}

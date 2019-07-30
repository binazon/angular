package com.orange.srs.refreport.business.delegate;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.orange.srs.statcommon.model.parameter.SupervisionMessage;
import com.orange.srs.statcommon.provider.messaging.JMSSupervisionDelegateInterface;
import com.orange.srs.statcommon.technical.supervision.JMSSupervisionState;

@Stateless
public class JMSSupervisionDelegate implements JMSSupervisionDelegateInterface {

	@EJB
	private JMSSupervisionState jmsSupervisionState;

	private static final Logger LOGGER = Logger.getLogger("Supervision");

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void supervisionMessageProcessing(ObjectMessage omessage) {
		try {
			SupervisionMessage supervisionMessage = (SupervisionMessage) omessage.getObject();
			LOGGER.debug("[JMSSupervisionDelegate] consumed message = " + supervisionMessage.getUuid() + " / from "
					+ supervisionMessage.getHostInfo() + " / date" + supervisionMessage.getSendDate());
			jmsSupervisionState.setConsumingAvailibility(true);
		} catch (Exception e) {
			jmsSupervisionState.setConsumingAvailibility(false);
			LOGGER.error("[JMSSupervisionDelegate] Cannot consume jms supervision message " + e.getMessage(), e);
		}
	}
}

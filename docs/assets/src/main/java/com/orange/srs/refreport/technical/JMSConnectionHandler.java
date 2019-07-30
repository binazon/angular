package com.orange.srs.refreport.technical;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.orange.srs.statcommon.consumer.messaging.JMSConnectionHelper;

@Singleton
@DependsOn("Configuration")
@Startup
public class JMSConnectionHandler extends JMSConnectionHelper {

	@PostConstruct
	private void construct() {
		String[] destinations = Configuration.producedChannels.split(",");
		construct(destinations);
	}

}

package com.orange.srs.refreport.technical;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

@Singleton
@Startup
@DependsOn("Log4jStartupBean")
public class Statistics {

	private static final Logger STAT = Logger.getLogger("statistics.refreport");

	public static final char LOG_SEPARATOR = '|';
	public static final char MSG_SEPARATOR = ';';

	public static final String REFREPORT = "REFREPORT";

	public static final String EXPORT_GRAPH_INVENTORY = "EXPORT_GRAPH_INVENTORY";
	public static final String ATTRIBUTES_PROVISIONING = "PROVISIONING_ATTRIBUTES";
	public static final String RETRIEVE_PROVISIONING_FILE = "RETRIEVE_PROVISIONING_FILE";
	public static final String DOD_REPORTING_GROUP_PROVISIONING = "DOD_REPORTING_GROUP_PROVISIONING";
	public static final String PROVISIONING = "PROVISIONING";
	public static final String EXPORT = "EXPORT";
	public static final String EXPORT_SPECIFIC_INVENTORY = "EXPORT_SPECIFIC_INVENTORY";
	public static final String FLUSH_GRAPH_DATABASES = "FLUSH_GRAPH_DATABASES";

	public static final String UNSET = "UNSET";
	public static final String ALL = "ALL";

	public static String HOSTINFO = "localhost|127.0.0.1";

	public Statistics() {

		try {
			InetAddress i = InetAddress.getLocalHost();
			HOSTINFO = i.getHostName() + LOG_SEPARATOR + i.getHostAddress();
		} catch (UnknownHostException uhe) {
		}
	}

	public static void addStatistics(String uuid, String application, String action, Long duration, String group,
			String origin, Object... actionMessage) {

		if (application == null || "".equals(application) || action == null || "".equals(action)) {
			return;
		}

		StringBuilder log = new StringBuilder(100);
		log.append(uuid).append(LOG_SEPARATOR);
		log.append(HOSTINFO).append(LOG_SEPARATOR);
		log.append(application).append(LOG_SEPARATOR);
		log.append((group == null) ? "" : group).append(LOG_SEPARATOR);
		log.append((origin == null) ? "" : origin).append(LOG_SEPARATOR);
		log.append(action).append(LOG_SEPARATOR);
		log.append((duration == null) ? "" : duration).append(LOG_SEPARATOR);
		log.append((actionMessage == null) ? "" : StringUtils.join(actionMessage, LOG_SEPARATOR));

		STAT.info(log);
	}
}

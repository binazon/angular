package com.orange.srs.refreport.technical.xml;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.orange.srs.statcommon.technical.xml.JAXBFactory;

@Singleton
@Startup
@DependsOn("Log4jStartupBean")
public class JAXBRefReportFactory extends JAXBFactory {

	public static final String REFREPORT_PARAMETER_PATH = "../classes/com/orange/srs/refreport/model/parameter";
	public static final String REFREPORT_INVENTORY_PARAMETER_PATH = "../classes/com/orange/srs/refreport/model/parameter/inventory";
	public static final String REFREPORT_INVENTORY_TO_PATH = "../classes/com/orange/srs/refreport/model/TO/inventory";
	public static final String REFREPORT_TO_PATH = "../classes/com/orange/srs/refreport/model/TO";
	public static final String REFREPORT_PROVISIONING_PARAMETER_PATH = "../classes/com/orange/srs/refreport/model/TO/provisioning";
	public static final String HTTP_JOB_PARAMETER_PATH = "/com/orange/srs/statcommon/model/parameter/jobparameter";
	public static final String REPORT_PARAMETER_PATH = "/com/orange/srs/statcommon/model/TO/report";
	public static final String PARTITION_PARAMETER_PATH = "/com/orange/srs/statcommon/model/TO/partition";
	public static final String REPORTCONF_PARAMETER_PATH = "/com/orange/srs/statcommon/model/TO/report/pollingConfiguration";
	public static final String FILTER_PARAMETER_PATH = "/com/orange/srs/statcommon/model/parameter/filter";
	public static final String PROVISIONING_PARAMETER_PATH = "/com/orange/srs/statcommon/model/parameter/provisioning";

	// public static final String
	// ADAPTER_PARAMETER_PATH="com/orange/srs/statcommon/model/TO";
	public static final String HTTP_JOB_PARAMETER_CLASS = "/com/orange/srs/statcommon/model/parameter/jobparameter/HttpJobParameter";

	@PostConstruct
	protected void loadContext() {
		String[] paths = { REFREPORT_PARAMETER_PATH, HTTP_JOB_PARAMETER_PATH, REFREPORT_PROVISIONING_PARAMETER_PATH,
				REPORT_PARAMETER_PATH, PARTITION_PARAMETER_PATH, REPORTCONF_PARAMETER_PATH, REFREPORT_TO_PATH,
				REFREPORT_INVENTORY_PARAMETER_PATH, FILTER_PARAMETER_PATH, REFREPORT_INVENTORY_TO_PATH,
				PROVISIONING_PARAMETER_PATH };
		String[] classes = { HTTP_JOB_PARAMETER_CLASS };
		loadContext(paths, classes);
	}
}

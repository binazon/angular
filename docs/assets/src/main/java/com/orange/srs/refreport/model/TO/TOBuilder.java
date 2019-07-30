package com.orange.srs.refreport.model.TO;

import com.orange.srs.refreport.model.Proxy;
import com.orange.srs.refreport.model.ReportOutput;
import com.orange.srs.statcommon.model.TO.report.GetProxyTO;
import com.orange.srs.statcommon.model.parameter.ReportOutputParameter;

public class TOBuilder {

	public static ReportOutputParameter buildReportOutputParameter(ReportOutput output) {
		ReportOutputParameter parameter = new ReportOutputParameter();
		parameter.format = output.getFormat();
		// IP2262
		parameter.compression = output.getCompression();
		parameter.outputPatternPrefix = output.getLocationPatternPrefix();
		parameter.outputPatternSuffix = output.getLocationPatternSuffix();
		parameter.outputUri = output.getUri();
		parameter.type = output.getType();
		return parameter;
	}

	public static GetProxyTO buildGetProxyTO(Proxy proxy) {
		GetProxyTO to = new GetProxyTO();
		to.proxyId = proxy.getPk();
		to.name = proxy.getName();
		to.uri = proxy.getUri();
		to.version = proxy.getVersion();
		return to;
	}
}

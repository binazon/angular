package com.orange.srs.refreport.model.TO;

public class ReportingGroupFilterUriTO {

	public Long reportingGroupPk;
	public String filterUri;

	public ReportingGroupFilterUriTO() {
	}

	public ReportingGroupFilterUriTO(Long reportingGroupPk, String filterUri) {
		this.reportingGroupPk = reportingGroupPk;
		this.filterUri = filterUri;
	}

}

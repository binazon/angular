package com.orange.srs.refreport.model.TO;

public class GroupReportConfigTO {

	public Long groupReportConfigPk;
	public Long reportingGroupPk;
	public String reportConfigAlias;
	public Long offerOptionPk;

	public GroupReportConfigTO() {
	}

	public GroupReportConfigTO(Long groupReportConfigPk, Long reportingGroupPk, String reportConfigAlias,
			Long offerOptionPk) {
		super();
		this.groupReportConfigPk = groupReportConfigPk;
		this.reportingGroupPk = reportingGroupPk;
		this.reportConfigAlias = reportConfigAlias;
		this.offerOptionPk = offerOptionPk;
	}

}

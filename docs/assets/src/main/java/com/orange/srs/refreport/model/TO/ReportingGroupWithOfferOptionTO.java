package com.orange.srs.refreport.model.TO;

public class ReportingGroupWithOfferOptionTO {

	public Long reportingGroupPk;
	public String reportingGroupRef;
	public String origin;
	public String offerOptionAlias;

	public ReportingGroupWithOfferOptionTO() {
	}

	public ReportingGroupWithOfferOptionTO(Long reportingGroupPk, String reportingGroupRef, String origin,
			String offerOptionAlias) {
		this.reportingGroupPk = reportingGroupPk;
		this.reportingGroupRef = reportingGroupRef;
		this.origin = origin;
		this.offerOptionAlias = offerOptionAlias;
	}

}

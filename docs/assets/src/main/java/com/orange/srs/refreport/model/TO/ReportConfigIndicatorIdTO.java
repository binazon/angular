package com.orange.srs.refreport.model.TO;

public class ReportConfigIndicatorIdTO {

	public Long reportConfigPk;
	public String indicatorId;

	public ReportConfigIndicatorIdTO() {
	}

	public ReportConfigIndicatorIdTO(Long reportConfigPk, String indicatorId) {
		super();
		this.reportConfigPk = reportConfigPk;
		this.indicatorId = indicatorId;
	}

}

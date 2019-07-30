package com.orange.srs.refreport.model.parameter;

import javax.xml.bind.annotation.XmlRootElement;

import com.orange.srs.statcommon.model.enums.ReportGranularityEnum;
import com.orange.srs.statcommon.model.enums.ReportTimeUnitEnum;

@XmlRootElement
public class UpdateHyperlinkParameter {

	private String label;

	private ReportGranularityEnum granularity;

	private ReportTimeUnitEnum reportTimeUnit;

	private String indicator;

	private String typeAlias;

	public String getIndicator() {
		return indicator;
	}

	public void setIndicator(String indicator) {
		this.indicator = indicator;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public ReportGranularityEnum getGranularity() {
		return granularity;
	}

	public void setGranularity(ReportGranularityEnum granularity) {
		this.granularity = granularity;
	}

	public ReportTimeUnitEnum getReportTimeUnit() {
		return reportTimeUnit;
	}

	public void setReportTimeUnit(ReportTimeUnitEnum reportTimeUnit) {
		this.reportTimeUnit = reportTimeUnit;
	}

	public String getTypeAlias() {
		return typeAlias;
	}

	public void setTypeAlias(String typeAlias) {
		this.typeAlias = typeAlias;
	}

}

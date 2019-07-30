package com.orange.srs.refreport.model.parameter;

import java.util.List;

import com.orange.srs.refreport.model.ReportInput;
import com.orange.srs.refreport.model.ReportOutput;

public class ProvionningReportParameter {

	private String refName;
	private String refId;
	private String label;
	private String computeUri;
	private String reportType;
	private String reportTimeUnit;

	private List<ReportOutput> listReportOutput;
	private List<ReportInput> listReportInput;

	public String getRefName() {
		return refName;
	}

	public void setRefName(String refName) {
		this.refName = refName;
	}

	public String getRefId() {
		return refId;
	}

	public void setRefId(String refId) {
		this.refId = refId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getComputeUri() {
		return computeUri;
	}

	public void setComputeUri(String computeUri) {
		this.computeUri = computeUri;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public String getReportTimeUnit() {
		return reportTimeUnit;
	}

	public void setReportTimeUnit(String reportTimeUnit) {
		this.reportTimeUnit = reportTimeUnit;
	}

	public List<ReportOutput> getListReportOutput() {
		return listReportOutput;
	}

	public void setListReportOutput(List<ReportOutput> listStatOutput) {
		this.listReportOutput = listStatOutput;
	}

	public List<ReportInput> getListReportInput() {
		return listReportInput;
	}

	public void setListReportInput(List<ReportInput> listReportInput) {
		this.listReportInput = listReportInput;
	}

}

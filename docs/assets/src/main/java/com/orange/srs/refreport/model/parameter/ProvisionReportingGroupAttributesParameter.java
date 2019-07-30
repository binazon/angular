package com.orange.srs.refreport.model.parameter;

import java.util.Calendar;

public class ProvisionReportingGroupAttributesParameter {
	public String fileName;
	public Calendar provisioningDate;
	public String origin;

	public Calendar getProvisioningDate() {
		return provisioningDate;
	}

	public void setProvisioningDate(Calendar provisioningDate) {
		this.provisioningDate = provisioningDate;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String GetFileName() {
		return fileName;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String GetOrigin() {
		return origin;
	}

	public ProvisionReportingGroupAttributesParameter(String origin, String fileName, Calendar provisioningDate) {
		this.origin = origin;
		this.fileName = fileName;
		this.provisioningDate = provisioningDate;
	}
}

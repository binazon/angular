package com.orange.srs.refreport.model.parameter;

import java.util.Calendar;

public class RetrieveProvisioningFilesForASourceParameter {
	public String source;
	public Calendar date;
	public String origin;

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setDate(Calendar date) {
		this.date = date;
	}

	public Calendar getDate() {
		return date;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getOrigin() {
		return origin;
	}

	public RetrieveProvisioningFilesForASourceParameter(String source, Calendar date, String origin) {
		this.source = source;
		this.date = date;
		this.origin = origin;
	}
}

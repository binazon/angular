package com.orange.srs.refreport.model.parameter;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CreateReportUserParameter {

	private long reportUserId;

	public long getReportUserId() {
		return reportUserId;
	}

	public void setReportUserId(long reportUserId) {
		this.reportUserId = reportUserId;
	}

}

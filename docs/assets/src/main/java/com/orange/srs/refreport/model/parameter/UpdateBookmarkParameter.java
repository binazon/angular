package com.orange.srs.refreport.model.parameter;

import javax.xml.bind.annotation.XmlRootElement;

import com.orange.srs.statcommon.model.enums.ReportGranularityEnum;
import com.orange.srs.statcommon.model.enums.ReportTimeUnitEnum;

@XmlRootElement
public class UpdateBookmarkParameter {

	private String bookmarkId;

	private String reportingEntityId;

	private ReportGranularityEnum granularity;

	private ReportTimeUnitEnum reportTimeUnit;

	private String filterId;

	private String offerOption;

	public String getBookmarkId() {
		return bookmarkId;
	}

	public void setBookmarkId(String bookmarkId) {
		this.bookmarkId = bookmarkId;
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

	public String getReportingEntityId() {
		return reportingEntityId;
	}

	public void setReportingEntityId(String reportingEntityId) {
		this.reportingEntityId = reportingEntityId;
	}

	public String getFilterId() {
		return filterId;
	}

	public void setFilterId(String filterId) {
		this.filterId = filterId;
	}

	public String getOfferOption() {
		return offerOption;
	}

	public void setOfferOption(String offerOption) {
		this.offerOption = offerOption;
	}

}

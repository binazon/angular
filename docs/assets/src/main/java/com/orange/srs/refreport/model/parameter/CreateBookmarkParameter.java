package com.orange.srs.refreport.model.parameter;

import javax.xml.bind.annotation.XmlRootElement;

import com.orange.srs.statcommon.model.enums.ReportGranularityEnum;
import com.orange.srs.statcommon.model.enums.ReportTimeUnitEnum;

@XmlRootElement
public class CreateBookmarkParameter {

	private String bookmarkId;

	private String indicator;

	private Long SRSId;

	private String reportingEntityId;

	private String reportingGroupName;

	private ReportGranularityEnum granularity;

	private ReportTimeUnitEnum reportTimeUnit;

	private String offerOption;

	private String origin;

	private String filterId;

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

	public String getBookmarkId() {
		return bookmarkId;
	}

	public void setBookmarkId(String bookmarkId) {
		this.bookmarkId = bookmarkId;
	}

	public String getReportingGroupName() {
		return reportingGroupName;
	}

	public void setReportingGroupName(String reportingGroupName) {
		this.reportingGroupName = reportingGroupName;
	}

	public String getReportingEntityId() {
		return reportingEntityId;
	}

	public void setReportingEntityId(String reportingEntityId) {
		this.reportingEntityId = reportingEntityId;
	}

	public Long getSrsId() {
		return SRSId;
	}

	public void setSRSId(Long SRSId) {
		this.SRSId = SRSId;
	}

	public String getIndicator() {
		return indicator;
	}

	public void setIndicator(String indicator) {
		this.indicator = indicator;
	}

	public String getOfferOption() {
		return offerOption;
	}

	public void setOfferOption(String offerOption) {
		this.offerOption = offerOption;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getFilterId() {
		return filterId;
	}

	public void setFilterId(String filterId) {
		this.filterId = filterId;
	}
}

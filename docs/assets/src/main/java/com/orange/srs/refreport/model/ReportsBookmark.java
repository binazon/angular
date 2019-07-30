package com.orange.srs.refreport.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.orange.srs.statcommon.model.enums.ReportGranularityEnum;
import com.orange.srs.statcommon.model.enums.ReportTimeUnitEnum;

@Entity
@Table(name = ReportsBookmark.TABLE_NAME)
public class ReportsBookmark extends Bookmark {

	public static final String TABLE_NAME = "T_REPORTS_BOOKMARK";

	public static final String COL_NAME_REPORTING_ENTITY_FK = "REPORTING_ENTITY_FK";

	public static final String FIELD_BOOKMARK_ID = "bookmarkId";
	public static final String FIELD_REPORTING_ENTITY = "reportingEntity";
	public static final String FIELD_REPORTING_GROUP = "reportingGroup";
	public static final String FIELD_REPORT_USER = "reportUser";
	public static final String FIELD_GRANULARITY = "granularity";
	public static final String FIELD_REPORT_TIME_UNIT = "reportTimeUnit";
	public static final String FIELD_FILTER = "filter";

	@Id
	@Column(name = "BOOKMARK_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long bookmarkId;

	@Column(name = "GRANULARITY", nullable = false)
	@Enumerated(value = EnumType.STRING)
	private ReportGranularityEnum granularity;

	@Column(name = "REPORT_TIME_UNIT", nullable = false)
	@Enumerated(value = EnumType.STRING)
	private ReportTimeUnitEnum reportTimeUnit;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COL_NAME_REPORTING_ENTITY_FK, nullable = true)
	private ReportingEntity reportingEntity;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "REPORT_USER_FK")
	private ReportUser reportUser;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "REPORTING_GROUP_FK")
	private ReportingGroup reportingGroup;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "FILTER_FK", nullable = true)
	private Filter filter;

	/**
	 * Get the property bookmarkId
	 * 
	 * @return the bookmarkId value
	 */
	public Long getBookmarkId() {
		return bookmarkId;
	}

	/**
	 * Set the property bookmarkId
	 * 
	 * @param bookmarkId
	 *            the bookmarkId to set
	 */
	public void setBookmarkId(Long bookmarkId) {
		this.bookmarkId = bookmarkId;
	}

	/**
	 * Get the property reportingEntity
	 * 
	 * @return the reportingGroup
	 */
	public ReportingEntity getReportingEntity() {
		return reportingEntity;
	}

	/**
	 * Set the property reportingEntity
	 * 
	 * @param reportingEntities
	 *            the reportingEntity to set
	 */
	public void setReportingEntity(ReportingEntity reportingEntity) {
		this.reportingEntity = reportingEntity;
	}

	/**
	 * Get the property reportUser
	 * 
	 * @return the reportUser
	 */
	public ReportUser getReportUser() {
		return reportUser;
	}

	/**
	 * Set the property reportUser
	 * 
	 * @param reportUsers
	 *            the ReportUser to set
	 */
	public void setReportUser(ReportUser reportUser) {
		this.reportUser = reportUser;
	}

	/**
	 * Get the property reportingGroup
	 * 
	 * @return the reportingGroup
	 */
	public ReportingGroup getReportingGroup() {

		return reportingGroup;
	}

	/**
	 * Set the property reportingGroup
	 * 
	 * @param reportingGroup
	 *            the reportingGroup to set
	 */
	public void setReportingGroup(ReportingGroup reportingGroup) {
		this.reportingGroup = reportingGroup;
	}

	/**
	 * Get the property granularity
	 * 
	 * @return the granularity value
	 */
	public ReportGranularityEnum getGranularity() {
		return granularity;
	}

	/**
	 * Set the property granularity
	 * 
	 * @param pk
	 *            the granularity to set
	 */
	public void setGranularity(ReportGranularityEnum granularity) {
		this.granularity = granularity;
	}

	/**
	 * Get the property reportTimeUnit
	 * 
	 * @return the reportTimeUnit value
	 */
	public ReportTimeUnitEnum getReportTimeUnit() {
		return reportTimeUnit;
	}

	/**
	 * Set the property reportTimeUnit
	 * 
	 * @param pk
	 *            the reportTimeUnit to set
	 */
	public void setReportTimeUnit(ReportTimeUnitEnum reportTimeUnit) {
		this.reportTimeUnit = reportTimeUnit;
	}

	/**
	 * Get the property filterId
	 * 
	 * @return the filter
	 */
	public Filter getFilter() {

		return filter;
	}

	/**
	 * Set the property reportingGroup
	 * 
	 * @param reportingGroup
	 *            the reportingGroup to set
	 */
	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof ReportsBookmark)) {
			return false;
		}
		ReportsBookmark other = (ReportsBookmark) obj;
		if (reportingEntity == null) {
			if (other.reportingEntity != null) {
				return false;
			}
		} else if (!reportingEntity.equals(other.reportingEntity)) {
			return false;
		}
		if (reportingGroup == null) {
			if (other.reportingGroup != null) {
				return false;
			}
		} else if (!reportingGroup.equals(other.reportingGroup)) {
			return false;
		}
		if (reportUser == null) {
			if (other.reportUser != null) {
				return false;
			}
		} else if (!reportUser.equals(other.reportUser)) {
			return false;
		} else if (!filter.equals(other.filter)) {
			return false;
		}

		return true;
	}

}

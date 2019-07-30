package com.orange.srs.refreport.model;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class ReportingGroupToEntitiesId implements Serializable {

	private static final long serialVersionUID = -4438126530151444647L;

	private Long reportingGroup;

	private Long reportingEntity;

	public ReportingGroupToEntitiesId() {
	}

	/**
	 * @param reportingGroupId
	 * @param statEntityId
	 */
	public ReportingGroupToEntitiesId(Long reportingGroupId, Long reportingEntityId) {
		this.reportingGroup = reportingGroupId;
		this.reportingEntity = reportingEntityId;
	}

	/**
	 * @return the reportingGroupId
	 */
	public Long getReportingGroupId() {
		return reportingGroup;
	}

	/**
	 * @param reportingGroupId
	 *            the reportingGroupId to set
	 */
	public void setReportingGroupId(Long reportingGroupId) {
		this.reportingGroup = reportingGroupId;
	}

	/**
	 * @return the statEntityId
	 */
	public Long getReportingEntityId() {
		return reportingEntity;
	}

	/**
	 * @param statEntityId
	 *            the statEntityId to set
	 */
	public void setReportingEntityId(Long reportingEntityId) {
		this.reportingEntity = reportingEntityId;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return reportingGroup + "," + reportingEntity;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((reportingEntity == null) ? 0 : reportingEntity.hashCode());
		result = prime * result + ((reportingGroup == null) ? 0 : reportingGroup.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportingGroupToEntitiesId other = (ReportingGroupToEntitiesId) obj;
		if (reportingEntity == null) {
			if (other.reportingEntity != null)
				return false;
		} else if (!reportingEntity.equals(other.reportingEntity))
			return false;
		if (reportingGroup == null) {
			if (other.reportingGroup != null)
				return false;
		} else if (!reportingGroup.equals(other.reportingGroup))
			return false;
		return true;
	}

}
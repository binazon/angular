package com.orange.srs.refreport.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
@DiscriminatorValue("composed")
public class ComposedReportingGroup extends ReportingGroup {

	private static final long serialVersionUID = -8466351706424404663L;

	@ManyToMany
	@JoinTable(name = "TJ_COMPOSED_REPORTING_GROUP", joinColumns = @JoinColumn(name = "COMPOSED_REPORTING_GROUP_FK"), inverseJoinColumns = @JoinColumn(name = "REPORTING_GROUP_FK"))
	private List<ReportingGroup> reportingGroups;

	/**
	 * Get the property reportingGroups
	 *
	 * @return the reportingGroups value
	 */
	public List<ReportingGroup> getReportingGroups() {
		if (reportingGroups == null) {
			reportingGroups = new ArrayList<ReportingGroup>();
		}
		return reportingGroups;
	}

	/**
	 * Set the property reportingGroups
	 *
	 * @param reportingGroups
	 *            the reportingGroups to set
	 */
	public void setReportingGroups(List<ReportingGroup> reportingGroups) {
		this.reportingGroups = reportingGroups;
	}

}

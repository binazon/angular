package com.orange.srs.refreport.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "T_REPORT_USER")
public class ReportUser {

	public static final String TJ_NAME_REPORTING_GROUP = "TJ_REPORT_USER_TO_REPORTING_GROUP";

	public static final String TJ_COL_NAME_REPORTING_GROUP = "WORKING_GROUP_FK";

	public static final String FIELD_PK = "pk";
	public static final String FIELD_WORKING_GROUP = "workingGroups";

	@Id
	@Column(name = "PK")
	private Long pk;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = TJ_NAME_REPORTING_GROUP, joinColumns = @JoinColumn(name = "REPORT_USER_FK"), inverseJoinColumns = @JoinColumn(name = TJ_COL_NAME_REPORTING_GROUP))
	private Set<ReportingGroup> workingGroups;

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public Set<ReportingGroup> getWorkingGroups() {
		if (this.workingGroups == null)
			workingGroups = new HashSet<>();
		return workingGroups;
	}

	public void setWorkingGroups(Set<ReportingGroup> workingGroups) {
		this.workingGroups = workingGroups;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pk == null) ? 0 : pk.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportUser other = (ReportUser) obj;
		if (pk == null) {
			if (other.pk != null)
				return false;
		} else if (!pk.equals(other.pk))
			return false;
		return true;
	}

}

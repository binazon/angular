package com.orange.srs.refreport.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class GroupAttributeId implements Serializable {

	private static final long serialVersionUID = 4426375823925095567L;

	public static final String COL_NAME_NAME = "NAME";
	public static final String COL_NAME_REPORTING_GROUP_FK = "REPORTING_GROUP_FK";

	@Column(name = COL_NAME_NAME, nullable = false)
	private String paramName;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COL_NAME_REPORTING_GROUP_FK)
	private ReportingGroup targetGroup;

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public ReportingGroup getTargetGroup() {
		return targetGroup;
	}

	public void setTargetGroup(ReportingGroup targetGroup) {
		this.targetGroup = targetGroup;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((paramName == null) ? 0 : paramName.hashCode());
		result = prime * result + ((targetGroup == null) ? 0 : targetGroup.hashCode());
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
		GroupAttributeId other = (GroupAttributeId) obj;
		if (paramName == null) {
			if (other.paramName != null)
				return false;
		} else if (!paramName.equals(other.paramName))
			return false;
		if (targetGroup == null) {
			if (other.targetGroup != null)
				return false;
		} else if (!targetGroup.equals(other.targetGroup))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GroupAttributeId [paramName=" + paramName + ", targetGroup=" + targetGroup + "]";
	}
}

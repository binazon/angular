package com.orange.srs.refreport.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class EntityGroupAttributeId implements Serializable {

	private static final long serialVersionUID = 7312152830048838413L;

	public static final String COL_NAME_REPORTING_ENTITY_FK = "REPORTING_ENTITY_FK";

	@Column(name = "NAME", nullable = false)
	private String paramName;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "REPORTING_GROUP_FK", nullable = false)
	private ReportingGroup targetGroup;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = COL_NAME_REPORTING_ENTITY_FK, nullable = false)
	private ReportingEntity targetEntity;

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

	public ReportingEntity getTargetEntity() {
		return targetEntity;
	}

	public void setTargetEntity(ReportingEntity targetEntity) {
		this.targetEntity = targetEntity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((paramName == null) ? 0 : paramName.hashCode());
		result = prime * result + ((targetEntity == null) ? 0 : targetEntity.hashCode());
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
		EntityGroupAttributeId other = (EntityGroupAttributeId) obj;
		if (paramName == null) {
			if (other.paramName != null)
				return false;
		} else if (!paramName.equals(other.paramName))
			return false;
		if (targetEntity == null) {
			if (other.targetEntity != null)
				return false;
		} else if (!targetEntity.equals(other.targetEntity))
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
		return "EntityGroupAttributeId [paramName=" + paramName + ", targetGroup=" + targetGroup + ", targetEntity="
				+ targetEntity + "]";
	}
}

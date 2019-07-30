package com.orange.srs.refreport.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.StringUtils;

@Embeddable
public class EntityLinkId implements Serializable {

	private static final long serialVersionUID = -6103515463445419742L;

	public static final String COL_NAME_REPORTING_ENTITY_DEST_FK = "REPORTING_ENTITY_DEST_FK";
	public static final String COL_NAME_REPORTING_ENTITY_SRC_FK = "REPORTING_ENTITY_SRC_FK";
	public static final String COL_NAME_ROLE = "ROLE";

	@ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.MERGE })
	@JoinColumn(name = COL_NAME_REPORTING_ENTITY_DEST_FK, nullable = false)
	private ReportingEntity linkedDestEntity;

	@ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.MERGE })
	@JoinColumn(name = COL_NAME_REPORTING_ENTITY_SRC_FK, nullable = false)
	private ReportingEntity linkedSrcEntity;

	@Column(name = COL_NAME_ROLE, nullable = false)
	private String role;

	public ReportingEntity getLinkedDestEntity() {
		return linkedDestEntity;
	}

	public void setLinkedDestEntity(ReportingEntity linkedDestEntity) {
		this.linkedDestEntity = linkedDestEntity;
	}

	public ReportingEntity getLinkedSrcEntity() {
		return linkedSrcEntity;
	}

	public void setLinkedSrcEntity(ReportingEntity linkedSrcEntity) {
		this.linkedSrcEntity = linkedSrcEntity;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((linkedDestEntity == null) ? 0 : linkedDestEntity.hashCode());
		result = prime * result + ((linkedSrcEntity == null) ? 0 : linkedSrcEntity.hashCode());
		result = prime * result + ((role == null) ? 0 : role.hashCode());
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
		EntityLinkId other = (EntityLinkId) obj;
		if (linkedDestEntity == null) {
			if (other.linkedDestEntity != null)
				return false;
		} else if (!linkedDestEntity.equals(other.linkedDestEntity))
			return false;
		if (linkedSrcEntity == null) {
			if (other.linkedSrcEntity != null)
				return false;
		} else if (!linkedSrcEntity.equals(other.linkedSrcEntity))
			return false;
		if (!StringUtils.equals(this.role, other.getRole())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "EntityLinkId [linkedDestEntity=" + linkedDestEntity + ", linkedSrcEntity=" + linkedSrcEntity + ", role="
				+ role + "]";
	}
}

package com.orange.srs.refreport.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

@Embeddable
public class EntityLinkAttributeId implements Serializable {

	private static final long serialVersionUID = -735986863412270552L;

	public static final String COL_NAME_NAME = "NAME";
	public static final String COL_NAME_REPORTING_ENTITY_DEST_FK = "REPORTING_ENTITY_DEST_FK";
	public static final String COL_NAME_REPORTING_ENTITY_SRC_FK = "REPORTING_ENTITY_SRC_FK";
	public static final String COL_NAME_ROLE = "ROLE";

	@Column(name = COL_NAME_NAME, nullable = false)
	private String paramName;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumns({
			@JoinColumn(name = COL_NAME_REPORTING_ENTITY_DEST_FK, referencedColumnName = EntityLinkId.COL_NAME_REPORTING_ENTITY_DEST_FK),
			@JoinColumn(name = COL_NAME_REPORTING_ENTITY_SRC_FK, referencedColumnName = EntityLinkId.COL_NAME_REPORTING_ENTITY_SRC_FK),
			@JoinColumn(name = COL_NAME_ROLE, referencedColumnName = EntityLinkId.COL_NAME_ROLE) })
	private EntityLink targetEntityLink;

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public EntityLink getTargetEntityLink() {
		return targetEntityLink;
	}

	public void setTargetEntityLink(EntityLink targetEntityLink) {
		this.targetEntityLink = targetEntityLink;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((paramName == null) ? 0 : paramName.hashCode());
		result = prime * result + ((targetEntityLink == null) ? 0 : targetEntityLink.hashCode());
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
		EntityLinkAttributeId other = (EntityLinkAttributeId) obj;
		if (paramName == null) {
			if (other.paramName != null)
				return false;
		} else if (!paramName.equals(other.paramName))
			return false;
		if (targetEntityLink == null) {
			if (other.targetEntityLink != null)
				return false;
		} else if (!targetEntityLink.equals(other.targetEntityLink))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EntityGroupAttributeId [paramName=" + paramName + ", targetEntityLink=" + targetEntityLink + "]";
	}
}

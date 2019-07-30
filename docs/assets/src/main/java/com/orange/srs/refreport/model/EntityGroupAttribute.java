package com.orange.srs.refreport.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = EntityGroupAttribute.TABLE_NAME)
public class EntityGroupAttribute {

	public static final String TABLE_NAME = "T_ENTITY_GROUP_ATTRIBUTE";

	public static final String FIELD_NAME = "entityGroupAttributeId.paramName";
	public static final String FIELD_VALUE = "paramValue";
	public static final String FIELD_REPORTING_GROUP = "entityGroupAttributeId.targetGroup";
	public static final String FIELD_REPORTING_ENTITY = "entityGroupAttributeId.targetEntity";

	@EmbeddedId
	private EntityGroupAttributeId entityGroupAttributeId;

	@Column(name = "VALUE")
	private String paramValue;

	public EntityGroupAttributeId getEntityGroupAttributeId() {
		return entityGroupAttributeId;
	}

	public void setEntityGroupAttributeId(EntityGroupAttributeId entityGroupAttributeId) {
		this.entityGroupAttributeId = entityGroupAttributeId;
	}

	public String getParamName() {
		if (entityGroupAttributeId == null) {
			entityGroupAttributeId = new EntityGroupAttributeId();
		}
		return entityGroupAttributeId.getParamName();
	}

	public void setParamName(String paramName) {
		if (entityGroupAttributeId == null) {
			entityGroupAttributeId = new EntityGroupAttributeId();
		}
		entityGroupAttributeId.setParamName(paramName);
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	public ReportingGroup getTargetGroup() {
		if (entityGroupAttributeId == null) {
			entityGroupAttributeId = new EntityGroupAttributeId();
		}
		return entityGroupAttributeId.getTargetGroup();
	}

	public void setTargetGroup(ReportingGroup targetGroup) {
		if (entityGroupAttributeId == null) {
			entityGroupAttributeId = new EntityGroupAttributeId();
		}
		entityGroupAttributeId.setTargetGroup(targetGroup);
	}

	public ReportingEntity getTargetEntity() {
		if (entityGroupAttributeId == null) {
			entityGroupAttributeId = new EntityGroupAttributeId();
		}
		return entityGroupAttributeId.getTargetEntity();
	}

	public void setTargetEntity(ReportingEntity targetEntity) {
		if (entityGroupAttributeId == null) {
			entityGroupAttributeId = new EntityGroupAttributeId();
		}
		entityGroupAttributeId.setTargetEntity(targetEntity);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityGroupAttributeId == null) ? 0 : entityGroupAttributeId.hashCode());
		result = prime * result + ((paramValue == null) ? 0 : paramValue.hashCode());
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
		EntityGroupAttribute other = (EntityGroupAttribute) obj;
		if (entityGroupAttributeId == null) {
			if (other.entityGroupAttributeId != null)
				return false;
		} else if (!entityGroupAttributeId.equals(other.entityGroupAttributeId))
			return false;
		if (paramValue == null) {
			if (other.paramValue != null)
				return false;
		} else if (!paramValue.equals(other.paramValue))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EntityGroupAttribute [entityGroupAttributeId=" + entityGroupAttributeId + ", paramValue=" + paramValue
				+ "]";
	}
}

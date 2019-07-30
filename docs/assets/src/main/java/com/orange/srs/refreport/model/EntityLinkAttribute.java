package com.orange.srs.refreport.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/*
 * EntityLinkAttribute: Parameter associated to a link between Reporting Entity (example: SLA parameters)
 * It is used for:
 * - statistics calculation (OpenReport)
 */

@Entity
@Table(name = EntityLinkAttribute.TABLE_NAME)
public class EntityLinkAttribute {

	public static final String TABLE_NAME = "T_ENTITY_LINK_ATTRIBUTE";

	public static final String COL_NAME_VALUE = "VALUE";

	public static final String FIELD_NAME = "entityLinkAttributeId.paramName";
	public static final String FIELD_VALUE = "paramValue";
	public static final String FIELD_ENTITY_LINK = "entityLinkAttributeId.targetEntityLink";

	@EmbeddedId
	private EntityLinkAttributeId entityLinkAttributeId;

	@Column(name = COL_NAME_VALUE)
	private String paramValue;

	public EntityLinkAttributeId getEntityLinkAttributeId() {
		return entityLinkAttributeId;
	}

	public void setEntityLinkAttributeId(EntityLinkAttributeId entityLinkAttributeId) {
		this.entityLinkAttributeId = entityLinkAttributeId;
	}

	public String getParamName() {
		if (entityLinkAttributeId == null) {
			entityLinkAttributeId = new EntityLinkAttributeId();
		}
		return entityLinkAttributeId.getParamName();
	}

	public void setParamName(String paramName) {
		if (entityLinkAttributeId == null) {
			entityLinkAttributeId = new EntityLinkAttributeId();
		}
		entityLinkAttributeId.setParamName(paramName);
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	public EntityLink getTargetEntity() {
		if (entityLinkAttributeId == null) {
			entityLinkAttributeId = new EntityLinkAttributeId();
		}
		return entityLinkAttributeId.getTargetEntityLink();
	}

	public void setTargetEntity(EntityLink targetEntityLink) {
		if (entityLinkAttributeId == null) {
			entityLinkAttributeId = new EntityLinkAttributeId();
		}
		entityLinkAttributeId.setTargetEntityLink(targetEntityLink);
	}
}

package com.orange.srs.refreport.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

import com.orange.srs.refreport.model.enumerate.EntityLinkRoleEnum;
import com.orange.srs.refreport.model.enumerate.EntityLinkTypeEnum;

@Entity
@Table(name = EntityLink.TABLE_NAME)
public class EntityLink {

	public static final String TABLE_NAME = "T_ENTITY_LINK";

	public static final String COL_NAME_PARAMETER = "PARAMETER";
	public static final String COL_NAME_TYPE = "TYPE";

	public static final String FIELD_ENTITYLINK_ID = "entityLinkId";
	public static final String FIELD_REPORTING_ENTITY_DEST = "entityLinkId.linkedDestEntity";
	public static final String FIELD_REPORTING_ENTITY_SRC = "entityLinkId.linkedSrcEntity";
	public static final String FIELD_TYPE = "entityLinkId.type";
	public static final String FIELD_ROLE = "role";
	public static final String FIELD_PARAMETER = "parameter";

	@EmbeddedId
	private EntityLinkId entityLinkId;

	@Column(name = COL_NAME_TYPE, nullable = false)
	private String type;

	/**
	 * Optional, empty for a "simple" link Additional parameter about the link (example IP address of a voice gateway
	 * seen in a ipbx zone
	 */
	@Column(name = COL_NAME_PARAMETER, nullable = true)
	private String parameter;

	@OneToMany(fetch = FetchType.LAZY, cascade = {
			CascadeType.REMOVE }, mappedBy = EntityLinkAttribute.FIELD_ENTITY_LINK)
	private List<EntityLinkAttribute> entityLinkAttributes;

	public EntityLink() {
		super();
	}

	public EntityLink(ReportingEntity linkedSrcEntity, ReportingEntity linkedDestEntity, EntityLinkTypeEnum type,
			EntityLinkRoleEnum role, String parameter) {
		super();
		this.entityLinkId = new EntityLinkId();
		this.entityLinkId.setLinkedSrcEntity(linkedSrcEntity);
		this.entityLinkId.setLinkedDestEntity(linkedDestEntity);
		this.entityLinkId.setRole(role.getValue());
		this.type = type.getValue();
		this.parameter = parameter;
	}

	public EntityLinkId getEntityLinkId() {
		return entityLinkId;
	}

	public void setEntityLinkId(EntityLinkId entityLinkId) {
		this.entityLinkId = entityLinkId;
	}

	public ReportingEntity getLinkedDestEntity() {
		if (entityLinkId == null) {
			entityLinkId = new EntityLinkId();
		}
		return entityLinkId.getLinkedDestEntity();
	}

	public void setLinkedDestEntity(ReportingEntity linkedDestEntity) {
		if (entityLinkId == null) {
			entityLinkId = new EntityLinkId();
		}
		entityLinkId.setLinkedDestEntity(linkedDestEntity);
	}

	public ReportingEntity getLinkedSrcEntity() {
		if (entityLinkId == null) {
			entityLinkId = new EntityLinkId();
		}
		return entityLinkId.getLinkedSrcEntity();
	}

	public void setLinkedSrcEntity(ReportingEntity linkedSrcEntity) {
		if (entityLinkId == null) {
			entityLinkId = new EntityLinkId();
		}
		entityLinkId.setLinkedSrcEntity(linkedSrcEntity);
	}

	public String getRole() {
		if (entityLinkId == null) {
			entityLinkId = new EntityLinkId();
		}
		return entityLinkId.getRole();
	}

	public void setRole(String role) {
		if (entityLinkId == null) {
			entityLinkId = new EntityLinkId();
		}
		entityLinkId.setRole(role);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public List<EntityLinkAttribute> getEntityLinkAttributes() {
		if (entityLinkAttributes == null) {
			entityLinkAttributes = new ArrayList<>();
		}
		return entityLinkAttributes;
	}

	public void setEntityLinkAttributes(List<EntityLinkAttribute> entityLinkAttributes) {
		this.entityLinkAttributes = entityLinkAttributes;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EntityLink other = (EntityLink) obj;
		if (!this.entityLinkId.equals(other.getEntityLinkId())) {
			return false;
		}
		if (!StringUtils.equals(this.type, other.getType())) {
			return false;
		}
		if (!StringUtils.equals(this.parameter, other.getParameter())) {
			return false;
		}
		return true;
	}

}

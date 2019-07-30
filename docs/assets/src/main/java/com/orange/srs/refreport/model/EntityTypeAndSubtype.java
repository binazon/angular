package com.orange.srs.refreport.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "T_ENTITY_TYPE_AND_SUBTYPE")
public class EntityTypeAndSubtype {

	public static final String ALL_TYPES_OR_SUBTYPES = "*";

	public static final String FIELD_TYPE = "entityTypeAndSubtypeId.type";
	public static final String FIELD_SUBTYPE = "entityTypeAndSubtypeId.subtype";

	@EmbeddedId
	private EntityTypeAndSubtypeId entityTypeAndSubtypeId = new EntityTypeAndSubtypeId();

	@Column(name = "COMMENT")
	private String comment;

	public void setTypeAndSubType(String type, String subtype) {
		entityTypeAndSubtypeId.setType(type);
		entityTypeAndSubtypeId.setSubtype(subtype);
	}

	public EntityTypeAndSubtypeId getEntityTypeAndSubtypeId() {
		return entityTypeAndSubtypeId;
	}

	public String getType() {
		return entityTypeAndSubtypeId.getType();
	}

	public String getSubtype() {
		return entityTypeAndSubtypeId.getSubtype();
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityTypeAndSubtypeId == null) ? 0 : entityTypeAndSubtypeId.hashCode());
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
		EntityTypeAndSubtype other = (EntityTypeAndSubtype) obj;
		if (entityTypeAndSubtypeId == null) {
			if (other.entityTypeAndSubtypeId != null)
				return false;
		} else if (!entityTypeAndSubtypeId.equals(other.entityTypeAndSubtypeId))
			return false;
		return true;
	}

}

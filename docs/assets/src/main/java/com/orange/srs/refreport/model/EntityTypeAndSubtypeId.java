package com.orange.srs.refreport.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.orange.srs.statcommon.technical.AlphanumComparator;

@Embeddable
public class EntityTypeAndSubtypeId implements Serializable, Comparable<EntityTypeAndSubtypeId> {

	private static final long serialVersionUID = -6940407653899283799L;

	@Column(name = "TYPE")
	private String type;
	@Column(name = "SUBTYPE")
	private String subtype;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((subtype == null) ? 0 : subtype.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		EntityTypeAndSubtypeId other = (EntityTypeAndSubtypeId) obj;
		if (subtype == null) {
			if (other.subtype != null)
				return false;
		} else if (!subtype.equals(other.subtype))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public int compareTo(EntityTypeAndSubtypeId o) {
		int typeComparison = AlphanumComparator.compareString(type, o.type);
		if (typeComparison != 0) {
			return typeComparison;
		}
		return AlphanumComparator.compareString(subtype, o.subtype);
	}

	@Override
	public String toString() {
		return "EntityTypeAndSubtypeId [type=" + type + ", subtype=" + subtype + "]";
	}

}

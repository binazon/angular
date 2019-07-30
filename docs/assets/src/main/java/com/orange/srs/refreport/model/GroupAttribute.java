package com.orange.srs.refreport.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = GroupAttribute.TABLE_NAME, indexes = {
		@Index(name = "INDEX_T_GROUP_ATTRIBUTE_NAME_VALUE", columnList = GroupAttributeId.COL_NAME_NAME + ", "
				+ GroupAttribute.COL_NAME_VALUE) })
public class GroupAttribute {

	public static final String TABLE_NAME = "T_GROUP_ATTRIBUTE";

	public static final String COL_NAME_VALUE = "VALUE";

	public static final String FIELD_NAME = "groupAttributeId.paramName";
	public static final String FIELD_VALUE = "paramValue";
	public static final String FIELD_REPORTING_GROUP = "groupAttributeId.targetGroup";

	@EmbeddedId
	private GroupAttributeId groupAttributeId;

	@Column(name = COL_NAME_VALUE, nullable = false)
	private String paramValue;

	public GroupAttributeId getGroupAttributeId() {
		return groupAttributeId;
	}

	public void setGroupAttributeId(GroupAttributeId groupAttributeId) {
		this.groupAttributeId = groupAttributeId;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	public String getParamName() {
		if (groupAttributeId == null) {
			groupAttributeId = new GroupAttributeId();
		}
		return groupAttributeId.getParamName();
	}

	public void setParamName(String paramName) {
		if (groupAttributeId == null) {
			groupAttributeId = new GroupAttributeId();
		}
		groupAttributeId.setParamName(paramName);
	}

	public ReportingGroup getTargetGroup() {
		if (groupAttributeId == null) {
			groupAttributeId = new GroupAttributeId();
		}
		return groupAttributeId.getTargetGroup();
	}

	public void setTargetGroup(ReportingGroup targetGroup) {
		if (groupAttributeId == null) {
			groupAttributeId = new GroupAttributeId();
		}
		groupAttributeId.setTargetGroup(targetGroup);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupAttributeId == null) ? 0 : groupAttributeId.hashCode());
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
		GroupAttribute other = (GroupAttribute) obj;
		if (groupAttributeId == null) {
			if (other.groupAttributeId != null)
				return false;
		} else if (!groupAttributeId.equals(other.groupAttributeId))
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
		return "GroupAttribute [groupAttributeId=" + groupAttributeId + ", paramValue=" + paramValue + "]";
	}
}

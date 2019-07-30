package com.orange.srs.refreport.model.parameter;

public class AddAttributeToEntityAndGroupParameter {
	private long entityId;
	private long groupId;
	private String attributeName;
	private String attributeValue;

	public long getEntityId() {
		return entityId;
	}

	public void setEntityId(long entityId) {
		this.entityId = entityId;
	}

	public long getGroupId() {
		return groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAttributeValue() {
		return attributeValue;
	}

	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}

	public AddAttributeToEntityAndGroupParameter(long entityId, long groupId, String attributeName,
			String attributeValue) {

		this.entityId = entityId;
		this.groupId = groupId;
		this.attributeName = attributeName;
		this.attributeValue = attributeValue;
	}
}

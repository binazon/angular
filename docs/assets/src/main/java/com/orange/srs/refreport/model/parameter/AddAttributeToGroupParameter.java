package com.orange.srs.refreport.model.parameter;

public class AddAttributeToGroupParameter {

	private long groupId;
	private String attributeName;
	private String attributeValue;

	public AddAttributeToGroupParameter(long groupId, String attributeName, String attributeValue) {

		this.groupId = groupId;
		this.attributeName = attributeName;
		this.attributeValue = attributeValue;
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
}

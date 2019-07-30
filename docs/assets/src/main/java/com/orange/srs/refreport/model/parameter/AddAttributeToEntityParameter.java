package com.orange.srs.refreport.model.parameter;

public class AddAttributeToEntityParameter {

	private long entityId;
	private String attributeName;
	private String attributeValue;

	public long getEntityId() {
		return entityId;
	}

	public void setEntityId(long entityId) {
		this.entityId = entityId;
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

	public AddAttributeToEntityParameter(long entityId, String attributeName, String attributeValue) {

		this.entityId = entityId;
		this.attributeName = attributeName;
		this.attributeValue = attributeValue;
	}

}

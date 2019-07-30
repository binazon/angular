package com.orange.srs.refreport.model.parameter.inventory;

public class CreateEdgeParameter {

	public String sourceId;
	public String sourceType;
	public String sourceOrigin;

	public String destinationId;
	public String destinationType;
	public String destinationOrigin;

	public String parameter;
	public String role;

	@Override
	public String toString() {
		return "CreateEdgeParameter [sourceId=" + sourceId + ", sourceType=" + sourceType + ", sourceOrigin="
				+ sourceOrigin + ", destinationId=" + destinationId + ", destinationType=" + destinationType
				+ ", destinationOrigin=" + destinationOrigin + ", parameter=" + parameter + ", role=" + role + "]";
	}

	public void clear() {
		sourceId = sourceType = sourceOrigin = destinationId = destinationType = destinationOrigin = parameter = role = null;
	}
}

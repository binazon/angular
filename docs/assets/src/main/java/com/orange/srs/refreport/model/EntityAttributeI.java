package com.orange.srs.refreport.model;

public interface EntityAttributeI {

	public String getParamName();

	public String getParamValue();

	public void setParamValue(String paramValue);

	public void setTargetEntity(ReportingEntity targetEntity);
}

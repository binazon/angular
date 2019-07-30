package com.orange.srs.refreport.model.enumerate;

/**
 * Type for dataLocation : DI
 */
public enum DataLocationTypeEnum {
	CODE("CODE");
	private final String value;

	private DataLocationTypeEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}

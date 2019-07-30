package com.orange.srs.refreport.model.enumerate;

/**
 * Type value for dataLocation : DI
 */
public enum DataLocationTypeValueEnum {
	DI("DI");
	private final String value;

	private DataLocationTypeValueEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}

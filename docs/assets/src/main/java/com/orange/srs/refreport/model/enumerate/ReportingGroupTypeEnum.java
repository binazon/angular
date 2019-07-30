package com.orange.srs.refreport.model.enumerate;

public enum ReportingGroupTypeEnum {
	AUTOMATIC("AUTOMATIC"), MANUAL("MANUAL");
	private final String value;

	private ReportingGroupTypeEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return this.value;
	}
}

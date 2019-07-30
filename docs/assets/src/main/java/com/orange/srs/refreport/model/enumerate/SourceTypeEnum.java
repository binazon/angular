package com.orange.srs.refreport.model.enumerate;

public enum SourceTypeEnum {
	DB("database"), FILE("file"), HTTP("http");
	private final String value;

	private SourceTypeEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}

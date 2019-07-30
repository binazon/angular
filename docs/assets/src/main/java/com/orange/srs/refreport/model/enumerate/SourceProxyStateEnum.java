package com.orange.srs.refreport.model.enumerate;

public enum SourceProxyStateEnum {
	OK("OK"), KO("KO");
	private final String value;

	private SourceProxyStateEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public static SourceProxyStateEnum valueOfDescription(String text) {
		if (text != null) {
			for (SourceProxyStateEnum enumm : SourceProxyStateEnum.values()) {
				if (text.equalsIgnoreCase(enumm.getValue())) {
					return enumm;
				}
			}
			// this method is unusual in that IllegalArgumentException is
			// possibly thrown not at its beginning, but at its end.
			throw new IllegalArgumentException("Cannot parse into an element of AuthModeEnum : '" + text + "'");
		}
		return null;
	}
}

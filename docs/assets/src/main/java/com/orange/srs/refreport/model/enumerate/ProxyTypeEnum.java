package com.orange.srs.refreport.model.enumerate;

public enum ProxyTypeEnum {
	PROXYCOLLECTOR("proxycollector");
	private final String value;

	private ProxyTypeEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public String toString() {
		return this.value;
	}

	public static ProxyTypeEnum valueOfDescription(String text) {
		if (text != null) {
			for (ProxyTypeEnum enumm : ProxyTypeEnum.values()) {
				if (text.equalsIgnoreCase(enumm.getValue())) {
					return enumm;
				}
			}
		}
		return null;
	}
}

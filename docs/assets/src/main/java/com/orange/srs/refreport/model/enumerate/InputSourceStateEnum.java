package com.orange.srs.refreport.model.enumerate;

/**
 * Authentication mode for statuser : NOPASSWORD, LOCAL, LDAP or ITT
 */
public enum InputSourceStateEnum {
	START("STARTED"), STOPPED("STOP"), DESACTIVATED("DESACTIVATED");
	private final String value;

	private InputSourceStateEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public static InputSourceStateEnum valueOfDescription(String text) {
		if (text != null) {
			for (InputSourceStateEnum enumm : InputSourceStateEnum.values()) {
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

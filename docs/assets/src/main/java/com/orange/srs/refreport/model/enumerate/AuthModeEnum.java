package com.orange.srs.refreport.model.enumerate;

/**
 * Authentication mode for User : NOPASSWORD, LOCAL, LDAP
 */
public enum AuthModeEnum {
	LOCAL("LOCAL"), NOPASSWORD("NOPASSWORD"), LDAP("LDAP");

	private final String value;

	/**
	 * Constructor
	 * 
	 * @param value
	 */
	private AuthModeEnum(String value) {
		this.value = value;
	}

	/**
	 * Get Enumerate Value
	 * 
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Get Enumerate from value
	 * 
	 * @return
	 */
	public static AuthModeEnum valueOfDescription(String text) {
		if (text != null) {
			for (AuthModeEnum enumm : AuthModeEnum.values()) {
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

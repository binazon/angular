package com.orange.srs.refreport.model.enumerate;

/**
 * User role for authentication
 */
public enum AuthRoleEnum {
	NONE("NONE"), DEFAULT("DEFAULT"), CUSTOM_DB("CUSTOM_DB"), DESIGN_TPL("DESIGN_TPL"), INVENTORY("INVENTORY"), ADMIN(
			"ADMIN");

	private final String value;

	/**
	 * Constructor
	 * 
	 * @param value
	 */
	private AuthRoleEnum(String value) {
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
	 * Get array of all values of this enumerate
	 * 
	 * @return
	 */
	public static final String[] stringValues() {
		String[] stringValues = new String[values().length];
		for (int j = 0; j < values().length; j++) {
			stringValues[j] = values()[j].getValue();
		}
		return stringValues;
	}
}

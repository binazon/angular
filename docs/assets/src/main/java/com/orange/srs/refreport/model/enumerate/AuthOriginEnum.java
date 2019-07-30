package com.orange.srs.refreport.model.enumerate;

/**
 * Authentication connection origin : DIRECT, BBX, HKH, OTHER
 */
public enum AuthOriginEnum {
	DIRECT("DIRECT"), BBX("BBX"), HKH("HKH"), OTHER("OTHER");

	private final String value;

	/**
	 * Constructor
	 * 
	 * @param value
	 */
	private AuthOriginEnum(String value) {
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
	public static AuthOriginEnum valueOfDescription(String text) {
		if (text != null) {
			for (AuthOriginEnum enumm : AuthOriginEnum.values()) {
				if (text.equalsIgnoreCase(enumm.getValue())) {
					return enumm;
				}
			}
			// this method is unusual in that IllegalArgumentException is
			// possibly thrown not at its beginning, but at its end.
			throw new IllegalArgumentException("Cannot parse into an element of AuthOriginEnum : '" + text + "'");
		}
		return null;
	}
}

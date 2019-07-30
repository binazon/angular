package com.orange.srs.refreport.model.enumerate;

public enum PeriodicityEnum {
	DAY("Day"), WEEK("Week"), MONTH("Month"), YEAR("Year");
	private final String value;

	private PeriodicityEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public String toString() {
		return this.value;
	}

	public static PeriodicityEnum valueOfDescription(String text) {
		if (text != null) {
			for (PeriodicityEnum enumm : PeriodicityEnum.values()) {
				if (text.equalsIgnoreCase(enumm.getValue())) {
					return enumm;
				}
			}
			// this method is unusual in that IllegalArgumentException is
			// possibly thrown not at its beginning, but at its end.
			throw new IllegalArgumentException(
					"Cannot parse into an element of " + PeriodicityEnum.class.getName() + " : '" + text + "'");
		}
		return null;
	}
}

package com.orange.srs.refreport.model.parameter;

public class FamilyReferenceParameter {
	private String family;
	private String reference;

	public FamilyReferenceParameter(String family, String reference) {
		this.family = family;
		this.reference = reference;
	}

	/**
	 * Get the property family
	 *
	 * @return the family value
	 */
	public String getFamily() {
		return family;
	}

	/**
	 * Set the property family
	 *
	 * @param family
	 *            the family to set
	 */
	public void setFamily(String family) {
		this.family = family;
	}

	/**
	 * Get the property reference
	 *
	 * @return the reference value
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * Set the property reference
	 *
	 * @param reference
	 *            the reference to set
	 */
	public void setReference(String reference) {
		this.reference = reference;
	}
}

package com.orange.srs.refreport.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "T_HYPERLINK")
public class Hyperlink extends Bookmark {

	public static final String FIELD_LABEL = "label";
	public static final String FIELD_ADDITIONAL_FIELD_PARAM_TYPE = "additionalParamType";

	@Id
	@Column(name = "LABEL", unique = true)
	private String label;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "ADDITIONAL_PARAM_TYPE_FK", nullable = true)
	private ParamType additionalParamType;

	/**
	 * Get the property label
	 * 
	 * @return the label value
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Set the property label
	 * 
	 * @param pk
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof Hyperlink)) {
			return false;
		}
		Hyperlink other = (Hyperlink) obj;
		if (label == null) {
			if (other.label != null) {
				return false;
			}
		} else if (!label.equals(other.label)) {
			return false;
		}

		return true;
	}

	/**
	 * Get the property AdditionalParamType
	 * 
	 * @return the AdditionalParamType
	 */
	public ParamType getAdditionalParamType() {
		return additionalParamType;
	}

	/**
	 * Set the property additionalParamType
	 * 
	 * @param additionalParamType
	 *            the additionalParamType to set
	 */
	public void setAdditionalParamType(ParamType additionalParamType) {
		this.additionalParamType = additionalParamType;
	}
}

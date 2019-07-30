package com.orange.srs.refreport.model;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class Bookmark {

	public static final String FIELD_INDICATOR = "indicator";
	public static final String FIELD_PARAM_TYPE = "paramType";
	public static final String FIELD_OFFER_OPTION = "offerOption";

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "INDICATOR_FK")
	private Indicator indicator;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "OFFER_OPTION_FK")
	private OfferOption offerOption;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "PARAM_TYPE_FK", nullable = false)
	private ParamType paramType;

	/**
	 * Get the property offerOption
	 * 
	 * @return the offerOption value
	 */
	public OfferOption getOfferOption() {
		return offerOption;
	}

	/**
	 * Set the property offerOption
	 * 
	 * @param offerOption
	 *            the offerOption to set
	 */
	public void setOfferOption(OfferOption offerOption) {
		this.offerOption = offerOption;
	}

	/**
	 * Get the property paramType
	 * 
	 * @return the paramType
	 */
	public ParamType getParamType() {
		return paramType;
	}

	/**
	 * Set the property reportingEntity
	 * 
	 * @param paramType
	 *            the paramType to set
	 */
	public void setParamType(ParamType paramType) {
		this.paramType = paramType;
	}

	/**
	 * Get the property indicators
	 * 
	 * @return the indicators List
	 */
	public Indicator getIndicator() {
		return indicator;
	}

	/**
	 * Set the property indicators
	 * 
	 * @param indicators
	 *            the indicators to set
	 */
	public void setIndicator(Indicator indicator) {
		this.indicator = indicator;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Bookmark)) {
			return false;
		}
		Bookmark other = (Bookmark) obj;

		if (indicator == null) {
			if (other.indicator != null) {
				return false;
			}
		} else if (!indicator.equals(other.indicator)) {
			return false;
		}

		return true;
	}
}

package com.orange.srs.refreport.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class FilterToOfferOptionId implements Serializable {

	private static final long serialVersionUID = -8817526975694395083L;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "FILTER_FK", nullable = false)
	private Filter filter;

	@ManyToOne(cascade = CascadeType.REFRESH)
	@JoinColumn(name = "OFFER_OPTION_FK", nullable = false)
	private OfferOption offerOption;

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public OfferOption getOfferOption() {
		return offerOption;
	}

	public void setOfferOption(OfferOption offerOption) {
		this.offerOption = offerOption;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((offerOption == null) ? 0 : offerOption.hashCode());
		result = prime * result + ((filter == null) ? 0 : filter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FilterToOfferOptionId other = (FilterToOfferOptionId) obj;
		if (offerOption == null) {
			if (other.offerOption != null)
				return false;
		} else if (!offerOption.equals(other.offerOption))
			return false;
		if (filter == null) {
			if (other.filter != null)
				return false;
		} else if (!filter.equals(other.filter))
			return false;
		return true;
	}

}
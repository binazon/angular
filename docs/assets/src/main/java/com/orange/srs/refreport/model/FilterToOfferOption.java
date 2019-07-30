package com.orange.srs.refreport.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "TJ_FILTER_TO_OFFER_OPTION")
public class FilterToOfferOption {

	public static final String FIELD_FILTER = "filterToOfferOptionId.filter";
	public static final String FIELD_OFFER_OPTION = "filterToOfferOptionId.offerOption";
	public static final String FIELD_DEFAULT_FOR_ALL_GROUPS = "defaultForAllGroups";

	@EmbeddedId
	private FilterToOfferOptionId filterToOfferOptionId = new FilterToOfferOptionId();

	@Column(name = "DEFAULT_FOR_ALL_GROUPS", nullable = false)
	private boolean defaultForAllGroups;

	public FilterToOfferOptionId getFilterToOfferOptionId() {
		return filterToOfferOptionId;
	}

	public void setFilterToOfferOptionId(FilterToOfferOptionId filterToOfferOptionId) {
		this.filterToOfferOptionId = filterToOfferOptionId;
	}

	public Filter getFilter() {
		return filterToOfferOptionId.getFilter();
	}

	public void setFilter(Filter filter) {
		filterToOfferOptionId.setFilter(filter);
	}

	public OfferOption getOfferOption() {
		return filterToOfferOptionId.getOfferOption();
	}

	public void setOfferOption(OfferOption offerOption) {
		filterToOfferOptionId.setOfferOption(offerOption);
	}

	public boolean isDefaultForAllGroups() {
		return defaultForAllGroups;
	}

	public void setDefaultForAllGroups(boolean defaultForAllGroups) {
		this.defaultForAllGroups = defaultForAllGroups;
	}

}

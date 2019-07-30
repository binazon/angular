package com.orange.srs.refreport.model.TO;

public class FilterToOfferOptionTO {

	public String offerOptionAlias;
	public String filterId;
	public boolean defaultForAllGroups;

	public FilterToOfferOptionTO() {
	}

	public FilterToOfferOptionTO(String optionAlias, String filterId) {
		this.offerOptionAlias = optionAlias;
		this.filterId = filterId;
	}

	public FilterToOfferOptionTO(String optionAlias, String filterId, boolean defaultForAllGroups) {
		this.offerOptionAlias = optionAlias;
		this.filterId = filterId;
		this.defaultForAllGroups = defaultForAllGroups;
	}

}

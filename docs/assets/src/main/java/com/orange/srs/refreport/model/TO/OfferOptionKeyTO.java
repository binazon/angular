package com.orange.srs.refreport.model.TO;

import com.orange.srs.statcommon.technical.AlphanumComparator;

public class OfferOptionKeyTO implements Comparable<OfferOptionKeyTO> {

	private String alias;

	public OfferOptionKeyTO(String alias) {
		this.alias = alias;
	}

	@Override
	public int compareTo(OfferOptionKeyTO o) {
		return AlphanumComparator.compareString(alias, o.alias);
	}

	@Override
	public String toString() {
		return "OfferOptionKeyTO [alias=" + alias + "]";
	}
}

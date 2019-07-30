package com.orange.srs.refreport.model.TO;

import com.orange.srs.statcommon.technical.AlphanumComparator;

public class OfferKeyTO implements Comparable<OfferKeyTO> {

	private String alias;

	public OfferKeyTO(String alias) {
		this.alias = alias;
	}

	@Override
	public int compareTo(OfferKeyTO o) {
		return AlphanumComparator.compareString(alias, o.alias);
	}

	@Override
	public String toString() {
		return "OfferKeyTO [alias=" + alias + "]";
	}
}

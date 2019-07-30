package com.orange.srs.refreport.model.TO;

import com.orange.srs.statcommon.technical.AlphanumComparator;

public class FilterKeyTO implements Comparable<FilterKeyTO> {

	private String filterId;

	public FilterKeyTO(String id) {
		this.filterId = id;
	}

	@Override
	public int compareTo(FilterKeyTO o) {
		return AlphanumComparator.compareString(filterId, o.filterId);
	}

	@Override
	public String toString() {
		return "FilterKeyTO [filterId=" + filterId + "]";
	}
}

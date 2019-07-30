package com.orange.srs.refreport.model.TO;

import com.orange.srs.statcommon.technical.AlphanumComparator;

public class IndicatorKeyTO implements Comparable<IndicatorKeyTO> {

	private String indicatorId;

	public IndicatorKeyTO(String id) {
		this.indicatorId = id;
	}

	@Override
	public int compareTo(IndicatorKeyTO o) {
		return AlphanumComparator.compareString(indicatorId, o.indicatorId);
	}

	@Override
	public String toString() {
		return "IndicatorKeyTO [indicatorId=" + indicatorId + "]";
	}
}

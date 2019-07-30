package com.orange.srs.refreport.model.TO;

import com.orange.srs.statcommon.technical.AlphanumComparator;

public class ExternalIndicatorKeyTO implements Comparable<ExternalIndicatorKeyTO> {

	private String label;

	public ExternalIndicatorKeyTO(String label) {
		this.label = label;
	}

	@Override
	public int compareTo(ExternalIndicatorKeyTO o) {
		return AlphanumComparator.compareString(label, o.label);
	}

	@Override
	public String toString() {
		return "ExternalIndicatorKeyTO [label=" + label + "]";
	}
}

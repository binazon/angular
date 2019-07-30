package com.orange.srs.refreport.model.TO;

import com.orange.srs.statcommon.technical.AlphanumComparator;

public class HyperlinkKeyTO implements Comparable<HyperlinkKeyTO> {

	private String label;

	public HyperlinkKeyTO(String label) {
		this.label = label;
	}

	@Override
	public int compareTo(HyperlinkKeyTO o) {
		return AlphanumComparator.compareString(label, o.label);
	}

	@Override
	public String toString() {
		return "HyperlinkKeyTO [label=" + label + "]";
	}
}

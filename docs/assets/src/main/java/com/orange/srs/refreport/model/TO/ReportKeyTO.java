package com.orange.srs.refreport.model.TO;

import com.orange.srs.statcommon.technical.AlphanumComparator;

public class ReportKeyTO implements Comparable<ReportKeyTO> {

	private String refId;

	public ReportKeyTO(String refId) {
		this.refId = refId;
	}

	@Override
	public int compareTo(ReportKeyTO o) {
		return AlphanumComparator.compareString(refId, o.refId);
	}

	@Override
	public String toString() {
		return "ReportKeyTO [refId=" + refId + "]";
	}
}

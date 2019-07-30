package com.orange.srs.refreport.model.TO;

import com.orange.srs.statcommon.technical.AlphanumComparator;

public class ReportConfigKeyTO implements Comparable<ReportConfigKeyTO> {

	private String alias;

	public ReportConfigKeyTO(String alias) {
		this.alias = alias;
	}

	@Override
	public int compareTo(ReportConfigKeyTO o) {
		return AlphanumComparator.compareString(alias, o.alias);
	}

	@Override
	public String toString() {
		return "ReportConfigKeyTO [alias=" + alias + "]";
	}
}

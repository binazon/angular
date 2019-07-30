package com.orange.srs.refreport.model.TO;

import com.orange.srs.statcommon.technical.AlphanumComparator;

public class ParamTypeKeyTO implements Comparable<ParamTypeKeyTO> {

	private String alias;

	public ParamTypeKeyTO(String alias) {
		this.alias = alias;
	}

	@Override
	public int compareTo(ParamTypeKeyTO o) {
		return AlphanumComparator.compareString(alias, o.alias);
	}

	@Override
	public String toString() {
		return "ParamTypeKeyTO [alias=" + alias + "]";
	}
}

package com.orange.srs.refreport.model.TO;

import com.orange.srs.statcommon.technical.AlphanumComparator;

public class CriteriaKeyTO implements Comparable<CriteriaKeyTO> {

	private String type;
	private String value;

	public CriteriaKeyTO(String type, String value) {
		this.type = type;
		this.value = value;
	}

	@Override
	public int compareTo(CriteriaKeyTO o) {
		int typeComparison = AlphanumComparator.compareString(type, o.type);
		if (typeComparison != 0) {
			return typeComparison;
		}
		return AlphanumComparator.compareString(value, o.value);
	}

	@Override
	public String toString() {
		return "CriteriaKeyTO [type=" + type + ", value=" + value + "]";
	}
}

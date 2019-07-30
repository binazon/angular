package com.orange.srs.refreport.model.TO;

import com.orange.srs.statcommon.technical.AlphanumComparator;

public class SourceClassKeyTO implements Comparable<SourceClassKeyTO> {

	private String sourceClass;

	public SourceClassKeyTO(String sourceClass) {
		this.sourceClass = sourceClass;
	}

	@Override
	public int compareTo(SourceClassKeyTO o) {
		return AlphanumComparator.compareString(sourceClass, o.sourceClass);
	}

	@Override
	public String toString() {
		return "SourceClassKeyTO [sourceClass=" + sourceClass + "]";
	}
}

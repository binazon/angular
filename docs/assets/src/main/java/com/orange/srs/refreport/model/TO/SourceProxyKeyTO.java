package com.orange.srs.refreport.model.TO;

import com.orange.srs.statcommon.model.TO.report.InputSourceKey;

public class SourceProxyKeyTO implements Comparable<SourceProxyKeyTO> {

	private InputSourceKey inputSourceKey;
	private int index;

	public SourceProxyKeyTO(InputSourceKey inputSourceKey, int index) {
		this.inputSourceKey = inputSourceKey;
		this.index = index;
	}

	@Override
	public int compareTo(SourceProxyKeyTO o) {
		int inputSourceKeyComparison = inputSourceKey.compareTo(o.inputSourceKey);
		if (inputSourceKeyComparison != 0) {
			return inputSourceKeyComparison;
		}
		return index - o.index;
	}

	@Override
	public String toString() {
		return "SourceProxyKeyTO [inputSourceKey=" + inputSourceKey + ", index=" + index + "]";
	}
}

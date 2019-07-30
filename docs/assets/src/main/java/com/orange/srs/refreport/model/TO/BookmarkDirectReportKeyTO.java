package com.orange.srs.refreport.model.TO;

import com.orange.srs.statcommon.technical.AlphanumComparator;

public class BookmarkDirectReportKeyTO implements Comparable<BookmarkDirectReportKeyTO> {

	private final String label;
	private final String indicatorId;
	private final String paramType;

	public BookmarkDirectReportKeyTO(String label, String indicatorId, String paramType) {
		this.label = label;
		this.indicatorId = indicatorId;
		this.paramType = paramType;
	}

	@Override
	public int compareTo(BookmarkDirectReportKeyTO o) {
		int compareLabelResult = AlphanumComparator.compareString(label, o.label);
		if (compareLabelResult != 0) {
			return compareLabelResult;
		}
		int compareindicatorIdResult = AlphanumComparator.compareString(indicatorId, o.indicatorId);
		if (compareindicatorIdResult != 0) {
			return compareindicatorIdResult;
		}
		return AlphanumComparator.compareString(paramType, o.paramType);
	}

	@Override
	public String toString() {
		return "BookmarkDirectReportKeyTO [label=" + label + ", indicatorId=" + indicatorId + ", paramType=" + paramType
				+ "]";
	}
}

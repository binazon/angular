package com.orange.srs.refreport.model.TO;

import com.orange.srs.statcommon.technical.AlphanumComparator;

public class InputColumnKeyTO implements Comparable<InputColumnKeyTO> {

	private String inputFormatType;
	private String columnName;
	private String type;

	public InputColumnKeyTO(String inputFormatType, String columnName, String type) {
		this.inputFormatType = inputFormatType;
		this.columnName = columnName;
		this.type = type;
	}

	@Override
	public int compareTo(InputColumnKeyTO o) {
		int inputFormatTypeComparison = AlphanumComparator.compareString(inputFormatType, o.inputFormatType);
		if (inputFormatTypeComparison != 0) {
			return inputFormatTypeComparison;
		}
		int columnNameComparison = AlphanumComparator.compareString(columnName, o.columnName);
		if (columnNameComparison != 0) {
			return columnNameComparison;
		}
		return AlphanumComparator.compareString(type, o.type);
	}

	@Override
	public String toString() {
		return "InputColumnKeyTO [inputFormatType=" + inputFormatType + ", columnName=" + columnName + ", type=" + type
				+ "]";
	}
}

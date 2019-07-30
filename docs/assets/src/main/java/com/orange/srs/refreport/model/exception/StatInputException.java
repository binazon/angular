package com.orange.srs.refreport.model.exception;

public class StatInputException extends RuntimeException {

	private static final long serialVersionUID = -119796046711038529L;

	public static final String STAT_INPUT_EXCEPTION_START = "Exception in ReportInput with Id ";
	public static final String NO_SUCH_STAT_INPUT = "No such file statinput in Datasource";
	public static final String STATINPUT_UNICITY_CONSTRAINT = "ReportInput must be unique by statType, granularity, sourceTimeUnit";
	public static final String STATINPUT_NULL_FORMAT_EXCEPTION = " ,format is null";
	public static final String STATINPUT_NULL_OR_NO_COLUMN_EXCEPTION = " ,format is linked to no column";

	public StatInputException() {
	}

	public StatInputException(String arg0) {
		super(arg0);
	}

	public StatInputException(Long pStatInputId, String arg0) {
		super(STAT_INPUT_EXCEPTION_START + pStatInputId + arg0);
	}

	public StatInputException(Throwable arg0) {
		super(arg0);
	}

	public StatInputException(Long pStatInputId, String arg0, Throwable arg1) {
		super(STAT_INPUT_EXCEPTION_START + pStatInputId + arg0, arg1);
	}

}

package com.orange.srs.refreport.model.exception;

public class ProvisionningException extends RuntimeException {

	private static final long serialVersionUID = -8077154508853161366L;

	public static final String NO_SUCH_STAT_TYPE = "Error 01";
	public static final String STATINPUT_UNICITY_CONSTRAINT = "ReportInput must be unique by statType";
	public static final String OBJECTYPEREF_REF_UNICITY_CONSTRAINT = "ObjectTypeRef ref occurence must be unique by ReportInput";
	public static final String UNKWNON_OFFER_EXCEPTION = "The offer is not defined (or is multiple) in database";
	public static final String GROUP_UNICITY_CONSTRAINT = "Reporting group must be unique by StatGroupRef, GroupingCriteria and Origin";

	public ProvisionningException() {
		// TODO Auto-generated constructor stub
	}

	public ProvisionningException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public ProvisionningException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public ProvisionningException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

}

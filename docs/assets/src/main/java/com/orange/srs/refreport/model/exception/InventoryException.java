package com.orange.srs.refreport.model.exception;

public class InventoryException extends RuntimeException {

	private static final long serialVersionUID = 8272826809226577713L;

	public static final String UNKWNON_TYPE_EXCEPTION = "Type is not present in configuration file";
	public static final String UNKWNON_GROUP_EXCEPTION = "Unknown or multiple Reporting Group";
	public static final String UNKWNON_VERSION_EXCEPTION = "Version is not present or is wrongly defined in configuration file";
	public static final String MULTIPLE_GROUP_EXCEPTION = "Multiple Reporting Group";
	public static final String NO_REPORTING_GROUP_EXCEPTION = "No reporting group found for this offerOption aliases : ";
	public static final String WRONG_SPECIFIC_INPUT_PARAMETER_EXCEPTION = "Wrong input parameter, missing data : ";

	public InventoryException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public InventoryException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public InventoryException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public InventoryException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}

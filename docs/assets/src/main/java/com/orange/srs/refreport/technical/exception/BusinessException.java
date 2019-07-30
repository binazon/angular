package com.orange.srs.refreport.technical.exception;

public class BusinessException extends Exception {

	private static final long serialVersionUID = 7010132443399559002L;

	public static String ENTITY_NOT_FOUND_EXCEPTION = "CANNOT FIND PERSISTENT ENTITY";
	public static String ENTITY_NOT_UNIQUE_EXCEPTION = "ENTITY UNICITY EXCEPTION";
	public static String WRONG_PARAMETER_EXCEPTION = "INCORRECT PARAMETERS";

	public static int WRONG_PARAMETER_EXCEPTION_CODE = 1;
	public static int ENTITY_NOT_FOUND_EXCEPTION_CODE = 2;
	public static int ENTITY_NOT_UNIQUE_EXCEPTION_CODE = 3;

	public static String UNSUPPORTED_EXTERNAL_SYSTEM_EXCEPTION = "Unknown external system";
	public static int UNSUPPORTED_EXTERNAL_SYSTEM_EXCEPTION_CODE = 4;

	public static int UNKNOWN_GRAPH_TO_REGISTRY_CODE = 5;
	public static String UNKNOWN_GRAPH__TO_REGISTRY_EXCEPTION = "GraphDatabase Service unknown to registry";

	public static int GRAPH_ALREADY_EXISTS_IN_REGISTRY_CODE = 6;
	public static String GRAPH_ALREADY_EXISTS_IN_REGISTRY_EXCEPTION = "GraphDatabase Service already exists in registry";

	public static int INCONSISTENT_GRAPH_STATE_FOR_OPERATION_CODE = 7;
	public static String INCONSISTENT_GRAPH_STATE_FOR_OPERATION_EXCEPTION = "GraphDatabase Service has wrong state for operation";

	public static int ILLEGAL_GRAPH_DATABASE_SERVICE_STATE_TRANSISTION_CODE = 8;
	public static String ILLEGAL_GRAPH_DATABASE_SERVICE_STATE_TRANSISTION_EXCEPTION = "Cannot change from old state to new state";

	public static int GRAPH_FOLDER_CREATION_EXCEPTION_CODE = 9;
	public static String GRAPH_FOLDER_CREATION_EXCEPTION = "Graph folder must not exists and be a directory";

	public static int UNKNOWN_GRAPH_CREATION_COMMAND = 10;
	public static String UNKNOWN_GRAPH_CREATION_COMMAND_EXCEPTION = "Graph command does not exists";

	public static int EDGE_CREATION_EXCEPTION = 11;
	public static String EDGE_CREATION_EXCEPTION_MESSAGE = "Cannot create edge ";

	public static int NO_GRAPH_LOADED_EXCEPTION = 12;
	public static String NO_GRAPH_LOADED_EXCEPTION_MESSAGE = "No Graph database loaded ";

	public static int NO_DATA_FOUND_EXCEPTION = 13;
	public static String NO_DATA_FOUND_EXCEPTION_MESSAGE = "No data found for ";

	public static int ERROR_PROVISIONING_FILE = 15;
	public static String ERROR_PROVISIONING_FILE_MESSAGE = "Error with the provisioning file: ";

	public int code;

	public BusinessException() {
		// TODO Auto-generated constructor stub
	}

	public BusinessException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public BusinessException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public BusinessException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public BusinessException(String message, int code) {
		super(message);
		this.code = code;
	}

	public BusinessException(String message, Throwable cause, int code) {
		super(message, cause);
		this.code = code;
	}

}

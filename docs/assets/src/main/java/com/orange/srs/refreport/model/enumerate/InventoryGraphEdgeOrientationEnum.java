package com.orange.srs.refreport.model.enumerate;

import org.neo4j.graphdb.RelationshipType;

public enum InventoryGraphEdgeOrientationEnum implements RelationshipType {
	OUTGOING("OUTGOING"), INCOMING("INCOMING"), NOT_ORIENTED("NOT_ORIENTED");

	private final String value;

	/**
	 * Constructor
	 * 
	 * @param value
	 */
	private InventoryGraphEdgeOrientationEnum(String value) {
		this.value = value;
	}

	/**
	 * Get Enumerate Value
	 * 
	 * @return
	 */
	public String getValue() {
		return value;
	}

}

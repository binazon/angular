package com.orange.srs.refreport.model.enumerate;

import org.neo4j.graphdb.RelationshipType;

public enum InventoryGraphEdgeTypeEnum implements RelationshipType {
	BELONGS_TO_GROUP("BELONGS_TO_GROUP"), IS_PARENT("IS_PARENT"), HAS_EQUIPMENT(
			"HAS_EQUIPMENT"), HAS_EQUIPMENT_ORIGINFDN("HAS_EQUIPMENT_ORIGINFDN"), HAS_EQUIPMENT_ENDFDN(
					"HAS_EQUIPMENT_ENDFDN"), HAS_MEASURE("HAS_MEASURE"), HAS_PHONE_ENTITY(
							"HAS_PHONE_ENTITY"), IS_LINKED_TO_EQUIPMENT(
									"IS_LINKED_TO_EQUIPMENT"), HAS_PHYSICALCONNECTION(
											"HAS_PHYSICALCONNECTION"), HAS_PHYSICALCONNECTION_ORIGINFDN(
													"HAS_PHYSICALCONNECTION_ORIGINFDN"), HAS_PHYSICALCONNECTION_ENDFDN(
															"HAS_PHYSICALCONNECTION_ENDFDN"), HAS_LOGICALCONNECTION(
																	"HAS_LOGICALCONNECTION"), HAS_LOGICALCONNECTION_ORIGINFDN(
																			"HAS_LOGICALCONNECTION_ORIGINFDN"), HAS_LOGICALCONNECTION_ENDFDN(
																					"HAS_LOGICALCONNECTION_ENDFDN"), HAS_SITE(
																							"HAS_SITE"), HAS_SITE_NOMINAL(
																									"HAS_SITE_NOMINAL"), HAS_SITE_BACKUP(
																											"HAS_SITE_BACKUP"), HAS_CITY(
																													"HAS_CITY"), HAS_TERMINAL(
																															"HAS_TERMINAL"), HAS_PATH(
																																	"HAS_PATH"), HAS_PATH_ORIGIN(
																																			"HAS_PATH_ORIGIN"), HAS_PATH_END(
																																					"HAS_PATH_END"), HAS_GKSITE(
																																							"HAS_GKSITE"), HAS_SAN(
																																									"HAS_SAN"), HAS_TRUNK(
																																											"HAS_TRUNK"), CLOUD_SERVICE(
																																													"CLOUD_SERVICE"), GRP_CLOUD_SERVICE(
																																															"GRP_CLOUD_SERVICE"), GRP_CLOUD_ACCESS(
																																																	"GRP_CLOUD_ACCESS");

	private final String value;

	/**
	 * Constructor
	 * 
	 * @param value
	 */
	private InventoryGraphEdgeTypeEnum(String value) {
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

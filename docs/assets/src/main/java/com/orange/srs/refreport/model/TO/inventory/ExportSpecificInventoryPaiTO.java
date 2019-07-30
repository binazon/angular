package com.orange.srs.refreport.model.TO.inventory;

public class ExportSpecificInventoryPaiTO {

	public String entityId;
	public String reportingGroupRef;
	public String origin;

	public ExportSpecificInventoryPaiTO(String entityId, String reportingGroupRef, String origin) {
		super();
		this.entityId = entityId;
		this.reportingGroupRef = reportingGroupRef;
		this.origin = origin;
	}

	@Override
	public String toString() {
		return "ExportSpecificInventoryPaiTO [entityId=" + entityId + ", reportingGroupRef=" + reportingGroupRef
				+ ", origin=" + origin + "]";
	}

}

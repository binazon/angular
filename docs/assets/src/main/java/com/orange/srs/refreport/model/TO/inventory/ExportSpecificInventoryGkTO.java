package com.orange.srs.refreport.model.TO.inventory;

public class ExportSpecificInventoryGkTO {

	public String entityId;
	public String label;

	public ExportSpecificInventoryGkTO(String entityId, String label) {
		super();
		this.entityId = entityId;
		this.label = label;
	}

	@Override
	public String toString() {
		return "ExportSpecificInventoryGkTO [entityId=" + entityId + ", label=" + label + "]";
	}

}

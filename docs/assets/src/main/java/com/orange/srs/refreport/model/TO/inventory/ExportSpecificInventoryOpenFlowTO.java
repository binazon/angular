package com.orange.srs.refreport.model.TO.inventory;

public class ExportSpecificInventoryOpenFlowTO {

	public String entityId;
	public String routerIp;

	public ExportSpecificInventoryOpenFlowTO(String entityId, String ipRouter) {
		super();
		this.entityId = entityId;
		this.routerIp = ipRouter;
	}

	@Override
	public String toString() {
		return "ExportSpecificInventoryOpenFlowTO [entityId=" + entityId + ", routerIp=" + routerIp + "]";
	}

}

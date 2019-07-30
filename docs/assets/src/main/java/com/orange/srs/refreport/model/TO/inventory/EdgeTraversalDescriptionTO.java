package com.orange.srs.refreport.model.TO.inventory;

import com.orange.srs.refreport.model.enumerate.InventoryGraphEdgeOrientationEnum;
import com.orange.srs.refreport.model.enumerate.InventoryGraphEdgeTypeEnum;

public class EdgeTraversalDescriptionTO {
	public InventoryGraphEdgeTypeEnum edgeType;
	public InventoryGraphEdgeOrientationEnum edgeOrientation;

	@Override
	public String toString() {
		return "EdgeTraversalDescriptionTO [edgeType=" + edgeType + ", edgeOrientation=" + edgeOrientation + "]";
	}
}

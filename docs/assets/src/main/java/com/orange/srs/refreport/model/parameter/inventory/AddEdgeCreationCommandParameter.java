package com.orange.srs.refreport.model.parameter.inventory;

import com.orange.srs.refreport.model.enumerate.InventoryGraphEdgeTypeEnum;

public class AddEdgeCreationCommandParameter {
	public String originType;
	public String destinationType;
	public String role;
	public String associatedRule;
	public InventoryGraphEdgeTypeEnum edgeCorrespondingType;

	public void clear() {
		originType = destinationType = associatedRule = null;
		edgeCorrespondingType = null;
	}

}

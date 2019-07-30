package com.orange.srs.refreport.model.parameter.inventory;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Direction;

import com.orange.srs.refreport.model.enumerate.InventoryGraphEdgeTypeEnum;

public class OrderedExpanderParameter {
	public Direction levelDirection;
	public Set<InventoryGraphEdgeTypeEnum> edgeTypes = new HashSet<InventoryGraphEdgeTypeEnum>();
}

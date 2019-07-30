package com.orange.srs.refreport.model.parameter.inventory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.orange.srs.refreport.model.enumerate.InventoryGraphEdgeTypeEnum;

@XmlRootElement
public class ParentEdgeDefinition {
	@XmlAttribute
	public String childEntityType;
	@XmlAttribute
	public String parentEntityType;
	@XmlAttribute
	public InventoryGraphEdgeTypeEnum edgeType;
}

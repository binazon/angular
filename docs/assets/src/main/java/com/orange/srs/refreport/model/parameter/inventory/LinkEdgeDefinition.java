package com.orange.srs.refreport.model.parameter.inventory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.orange.srs.refreport.model.enumerate.InventoryGraphEdgeTypeEnum;

@XmlRootElement
public class LinkEdgeDefinition {
	@XmlAttribute
	public String sourceEntityType;
	@XmlAttribute
	public String destinationEntityType;
	@XmlAttribute
	public String role;
	@XmlAttribute
	public InventoryGraphEdgeTypeEnum edgeType;
	@XmlAttribute
	public String parameterRule;
}

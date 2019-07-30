package com.orange.srs.refreport.model.parameter.inventory;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "graphExportConfiguration")
public class InventoryGraphCreationParameter {

	@XmlElementWrapper(name = "parentEdgeDefinitions")
	@XmlElement(name = "parentEdgeBinding")
	public List<ParentEdgeDefinition> parentEdgeDefinition;

	@XmlElementWrapper(name = "linkEdgeDefinitions")
	@XmlElement(name = "linkEdgeBinding")
	public List<LinkEdgeDefinition> linkEdgeDefinition;

}

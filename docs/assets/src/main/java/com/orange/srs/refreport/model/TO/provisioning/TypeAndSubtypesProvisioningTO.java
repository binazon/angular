package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "typeAndSubtype")
public class TypeAndSubtypesProvisioningTO {

	@XmlAttribute(required = true)
	public String type;

	@XmlElement(name = "subtype")
	public List<SubtypeProvisioningTO> subtypeProvisioningTOs = new ArrayList<SubtypeProvisioningTO>();

	@XmlAttribute(required = false)
	public Boolean suppress;

}

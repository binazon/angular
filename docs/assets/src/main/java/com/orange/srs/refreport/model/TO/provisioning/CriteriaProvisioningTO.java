package com.orange.srs.refreport.model.TO.provisioning;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "criteria")
public class CriteriaProvisioningTO {

	@XmlAttribute(required = true)
	public String type;
	@XmlAttribute(required = true)
	public String value;

	public CriteriaProvisioningTO() {
	}

	public CriteriaProvisioningTO(String type, String value) {
		this.type = type;
		this.value = value;
	}
}

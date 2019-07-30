package com.orange.srs.refreport.model.TO.provisioning;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "subtype")
public class SubtypeProvisioningTO {

	@XmlAttribute(required = true)
	public String value;

	@XmlAttribute
	public String comment;

	@XmlAttribute(required = false)
	public Boolean suppress;
}

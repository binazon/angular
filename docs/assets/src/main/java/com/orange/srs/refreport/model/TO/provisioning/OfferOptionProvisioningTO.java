package com.orange.srs.refreport.model.TO.provisioning;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "option")
public class OfferOptionProvisioningTO {

	@XmlAttribute(required = true)
	public String label;
	@XmlAttribute(required = true)
	public String cpltAlias;
	@XmlAttribute(required = true)
	public String type;

	@XmlAttribute(required = false)
	public Boolean suppress;
}

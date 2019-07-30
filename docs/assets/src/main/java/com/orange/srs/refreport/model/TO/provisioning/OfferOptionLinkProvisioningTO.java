package com.orange.srs.refreport.model.TO.provisioning;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "offerOption")
public class OfferOptionLinkProvisioningTO {

	@XmlAttribute(required = true)
	public String alias;

	@XmlAttribute(required = false)
	public boolean defaultForAllGroups = false;

	@XmlAttribute(required = false)
	public Boolean suppress;
}

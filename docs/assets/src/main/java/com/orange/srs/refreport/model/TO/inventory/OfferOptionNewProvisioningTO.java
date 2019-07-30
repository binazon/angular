package com.orange.srs.refreport.model.TO.inventory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "offerOption")
public class OfferOptionNewProvisioningTO {

	@XmlAttribute(required = true)
	public String alias;

	public OfferOptionNewProvisioningTO() {
	}

	public OfferOptionNewProvisioningTO(String alias) {
		this.alias = alias;
	}
}

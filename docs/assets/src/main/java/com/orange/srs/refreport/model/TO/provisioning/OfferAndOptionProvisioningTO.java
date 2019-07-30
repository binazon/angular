package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "offer")
public class OfferAndOptionProvisioningTO {

	@XmlAttribute(required = true)
	public String commercialName;
	@XmlAttribute(required = true)
	public String alias;

	@XmlAttribute(required = false)
	public Boolean suppress;

	@XmlElement(name = "option")
	public List<OfferOptionProvisioningTO> offerOptionProvisioningTOs = new ArrayList<>();
}

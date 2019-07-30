package com.orange.srs.refreport.model.TO.provisioning;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "indicator")
public class IndicatorProvisioningTO {

	@XmlAttribute(required = true)
	public String id;
	@XmlAttribute(required = true)
	public String label;

	@XmlAttribute(required = false)
	public Boolean suppress;

	public IndicatorProvisioningTO() {
	}

	public IndicatorProvisioningTO(String id, String label) {
		this.id = id;
		this.label = label;
	}
}

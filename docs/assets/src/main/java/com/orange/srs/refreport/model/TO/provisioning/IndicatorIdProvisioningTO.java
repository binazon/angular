package com.orange.srs.refreport.model.TO.provisioning;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "indicator")
public class IndicatorIdProvisioningTO {

	@XmlAttribute(required = true)
	public String id;

	@XmlAttribute(required = false)
	public Boolean suppress;
}

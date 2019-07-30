package com.orange.srs.refreport.model.TO.provisioning;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "paramType")
public class ParamTypeAliasProvisioningTO {

	@XmlAttribute(required = true)
	public String alias;

	@XmlAttribute(required = false)
	public Boolean suppress;
}

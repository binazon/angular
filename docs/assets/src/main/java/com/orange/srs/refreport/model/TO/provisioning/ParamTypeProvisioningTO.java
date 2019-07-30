package com.orange.srs.refreport.model.TO.provisioning;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "paramType")
public class ParamTypeProvisioningTO {

	@XmlAttribute(required = true)
	public String alias;
	@XmlAttribute(required = true)
	public String entityType;
	@XmlAttribute(required = true)
	public String entitySubtype;
	@XmlAttribute(required = true)
	public String parentType;
	@XmlAttribute(required = true)
	public String parentSubtype;

	@XmlAttribute(required = false)
	public Boolean suppress;

	public ParamTypeProvisioningTO() {
	}

	public ParamTypeProvisioningTO(String alias, String entityType, String entitySubtype, String parentType,
			String parentSubtype) {
		this.alias = alias;
		this.entityType = entityType;
		this.entitySubtype = entitySubtype;
		this.parentType = parentType;
		this.parentSubtype = parentSubtype;
	}
}

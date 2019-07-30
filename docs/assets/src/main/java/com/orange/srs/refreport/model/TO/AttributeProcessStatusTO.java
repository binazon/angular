package com.orange.srs.refreport.model.TO;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "attribute")
public class AttributeProcessStatusTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5935632395123146116L;

	@XmlAttribute(required = false)
	public String ownerEntityId;

	@XmlAttribute(required = false)
	public String ownerGroupId;

	@XmlAttribute
	public String origin;

	@XmlAttribute(required = false)
	public String value;

	@XmlAttribute(required = false)
	public String oldvalue;

	@XmlAttribute(required = false)
	public String newvalue;

	@XmlAttribute(required = false)
	public String cause;

	public AttributeProcessStatusTO() {
	}

}

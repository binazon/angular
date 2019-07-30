package com.orange.srs.refreport.model.TO.provisioning;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.orange.srs.statcommon.technical.xml.CDATAXmlAdapter;

@XmlRootElement(name = "inputColumn")
public class InputColumnProvisioningTO {

	@XmlElement(required = true)
	public String columnName;
	@XmlElement(required = true)
	public String alias;
	@XmlElement(required = true)
	public String dataFormat;
	@XmlElement(required = true)
	@XmlJavaTypeAdapter(CDATAXmlAdapter.class)
	public String comments;
	@XmlElement(required = true)
	public String type;
	@XmlElement(required = false)
	public String defaultValue;

	@XmlAttribute(required = false)
	public Boolean suppress;

	public InputColumnProvisioningTO() {
	}

	public InputColumnProvisioningTO(String columnName, String alias, String dataFormat, String type,
			String defaultValue, String comments) {
		this.columnName = columnName;
		this.alias = alias;
		this.dataFormat = dataFormat;
		this.type = type;
		this.comments = comments;
		this.defaultValue = defaultValue;
	}
}

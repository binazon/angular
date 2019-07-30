package com.orange.srs.refreport.model.TO.provisioning;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "hyperlink")
public class HyperlinkProvisioningTO {

	@XmlAttribute(required = true)
	public String label;
	@XmlElement(required = true)
	public String indicatorId;
	@XmlElement(required = true)
	public String offerOptionAlias;
	@XmlElement(required = true)
	public String paramTypeAlias;
	@XmlElement(required = false)
	public String additionalParamTypeAlias;

	@XmlAttribute(required = false)
	public Boolean suppress;

	public HyperlinkProvisioningTO() {
	}

	public HyperlinkProvisioningTO(String label, String indicatorId, String offerOptionAlias, String paramTypeAlias,
			String additionalParamTypeAlias) {
		this.label = label;
		this.indicatorId = indicatorId;
		this.offerOptionAlias = offerOptionAlias;
		this.paramTypeAlias = paramTypeAlias;
		this.additionalParamTypeAlias = additionalParamTypeAlias;
	}
}

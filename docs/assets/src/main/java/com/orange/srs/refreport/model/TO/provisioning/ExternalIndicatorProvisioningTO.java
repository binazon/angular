package com.orange.srs.refreport.model.TO.provisioning;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.orange.srs.statcommon.model.enums.ComputeScopeEnum;

@XmlRootElement(name = "externalIndicator")
public class ExternalIndicatorProvisioningTO {

	@XmlAttribute(required = true)
	public String label;
	@XmlElement(required = true)
	public String indicatorId;
	@XmlElement(required = true)
	public String offerOptionAlias;
	@XmlElement(required = true)
	public String paramTypeAlias;
	@XmlElement(required = true)
	public String computeScope;

	@XmlAttribute(required = false)
	public Boolean suppress;

	public ExternalIndicatorProvisioningTO() {
	}

	public ExternalIndicatorProvisioningTO(String label, ComputeScopeEnum computeScope, String indicatorId,
			String offerOptionAlias, String paramTypeAlias) {
		this.label = label;
		this.computeScope = computeScope.name();
		this.indicatorId = indicatorId;
		this.offerOptionAlias = offerOptionAlias;
		this.paramTypeAlias = paramTypeAlias;
	}
}

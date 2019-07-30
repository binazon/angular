package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listExternalIndicator")
public class ExternalIndicatorListProvisioningTO {

	@XmlElement(name = "externalIndicator")
	public List<ExternalIndicatorProvisioningTO> externalIndicatorProvisioningTOs = new ArrayList<>();
}

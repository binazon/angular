package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listHyperlink")
public class HyperlinkListProvisioningTO {

	@XmlElement(name = "hyperlink")
	public List<HyperlinkProvisioningTO> hyperlinkProvisioningTOs = new ArrayList<HyperlinkProvisioningTO>();
}

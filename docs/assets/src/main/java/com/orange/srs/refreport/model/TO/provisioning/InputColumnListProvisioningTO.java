package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listInputColumn")
public class InputColumnListProvisioningTO {

	@XmlElement(name = "inputColumn")
	public List<InputColumnProvisioningTO> inputColumnProvisioningTOs = new ArrayList<InputColumnProvisioningTO>();
}

package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listSourceClass")
public class SourceClassListProvisioningTO {

	@XmlElement(name = "sourceClass")
	public List<SourceClassProvisioningTO> sourceClassProvisioningTOs = new ArrayList<SourceClassProvisioningTO>();
}

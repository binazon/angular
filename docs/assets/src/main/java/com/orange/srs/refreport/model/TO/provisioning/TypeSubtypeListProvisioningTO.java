package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listTypeSubtype")
public class TypeSubtypeListProvisioningTO {

	@XmlElement(name = "typeAndSubtype")
	public List<TypeAndSubtypesProvisioningTO> typeAndSubtypesProvisioningTOs = new ArrayList<TypeAndSubtypesProvisioningTO>();
}

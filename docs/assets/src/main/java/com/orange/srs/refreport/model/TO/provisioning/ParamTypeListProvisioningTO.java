package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listParamType")
public class ParamTypeListProvisioningTO {

	@XmlElement(name = "paramType")
	public List<ParamTypeProvisioningTO> paramTypeProvisioningTOs = new ArrayList<ParamTypeProvisioningTO>();
}

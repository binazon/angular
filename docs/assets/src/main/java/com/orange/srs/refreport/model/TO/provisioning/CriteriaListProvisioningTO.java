package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listCriteria")
public class CriteriaListProvisioningTO {

	@XmlElement(name = "criteria")
	public List<CriteriaProvisioningTO> criteriaProvisioningTOs = new ArrayList<CriteriaProvisioningTO>();
}

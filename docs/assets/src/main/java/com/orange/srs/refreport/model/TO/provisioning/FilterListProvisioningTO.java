package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listFilter")
public class FilterListProvisioningTO {

	@XmlElement(name = "filter")
	public List<FilterProvisioningTO> filterProvisioningTOs = new ArrayList<>();
}

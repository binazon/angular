package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listIndicator")
public class IndicatorListProvisioningTO {

	@XmlElement(name = "indicator")
	public List<IndicatorProvisioningTO> indicatorProvisioningTOs = new ArrayList<IndicatorProvisioningTO>();
}

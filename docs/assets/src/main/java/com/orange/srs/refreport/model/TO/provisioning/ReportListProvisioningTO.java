package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listReport")
public class ReportListProvisioningTO {

	@XmlElement(name = "report")
	public List<ReportProvisioningTO> reportProvisioningTOs = new ArrayList<ReportProvisioningTO>();
}

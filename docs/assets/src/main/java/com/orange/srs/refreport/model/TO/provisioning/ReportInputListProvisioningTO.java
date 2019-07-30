package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listReportInput")
public class ReportInputListProvisioningTO {

	@XmlElement(name = "reportInput")
	public List<ReportInputProvisioningTO> reportInputProvisioningTOs = new ArrayList<ReportInputProvisioningTO>();
}

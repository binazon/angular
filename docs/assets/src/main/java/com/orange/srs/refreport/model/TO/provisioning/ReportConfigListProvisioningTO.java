package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listReportConfig")
public class ReportConfigListProvisioningTO {

	@XmlElement(name = "reportConfig")
	public List<ReportConfigProvisioningTO> reportConfigProvisioningTOs = new ArrayList<ReportConfigProvisioningTO>();
}

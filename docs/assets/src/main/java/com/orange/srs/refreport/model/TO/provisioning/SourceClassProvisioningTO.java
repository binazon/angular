package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "sourceClass")
public class SourceClassProvisioningTO {

	@XmlAttribute(required = true)
	public String sourceClass;

	@XmlAttribute(required = false)
	public Boolean suppress;

	@XmlElementWrapper(name = "listReportInput")
	@XmlElement(name = "reportInput")
	public List<ReportInputKeyProvisioningTO> reportInputKeyProvisioningTOs = new ArrayList<ReportInputKeyProvisioningTO>();

}

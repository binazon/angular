package com.orange.srs.refreport.model.TO.provisioning;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.orange.srs.statcommon.model.enums.ReportGranularityEnum;
import com.orange.srs.statcommon.model.enums.ReportTimeUnitEnum;

@XmlRootElement(name = "report")
public class ReportProvisioningTO {

	@XmlTransient
	public Long reportPk;
	@XmlElement(required = true)
	public String refId;
	@XmlElement(required = true)
	public String label;
	@XmlElement(required = true)
	public String computeUri;
	@XmlElement(required = true)
	public String reportTimeUnit;
	@XmlElement(required = true)
	public String granularity;

	@XmlAttribute(required = false)
	public Boolean suppress;

	public ReportProvisioningTO() {
	}

	public ReportProvisioningTO(Long reportPk, String refId, String label, String computeUri,
			ReportTimeUnitEnum reportTimeUnit, ReportGranularityEnum granularity) {
		this.reportPk = reportPk;
		this.refId = refId;
		this.label = label;
		this.computeUri = computeUri;
		this.reportTimeUnit = reportTimeUnit.name();
		this.granularity = granularity.name();
	}
}
